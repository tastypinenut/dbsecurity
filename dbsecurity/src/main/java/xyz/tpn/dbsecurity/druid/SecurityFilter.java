package xyz.tpn.dbsecurity.druid;

import com.alibaba.druid.filter.FilterEventAdapter;
import com.alibaba.druid.proxy.jdbc.ResultSetProxy;
import com.alibaba.druid.proxy.jdbc.StatementProxy;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLUpdateSetItem;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlDeleteStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateStatement;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.util.JdbcConstants;
import com.mysql.cj.BindValue;
import com.mysql.cj.jdbc.ClientPreparedStatement;
import com.mysql.cj.jdbc.result.ResultSetImpl;
import com.mysql.cj.protocol.ResultsetRows;
import com.mysql.cj.result.Field;
import com.mysql.cj.result.Row;
import com.mysql.cj.util.StringUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import xyz.tpn.dbsecurity.SecurityHandler;
import xyz.tpn.dbsecurity.constant.SymbolConstants;
import xyz.tpn.dbsecurity.exception.DBEncryptException;
import xyz.tpn.dbsecurity.exception.TableColumnException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class SecurityFilter extends FilterEventAdapter {
    /**
     * 加密是否开启，默认开启
     */
    @Setter
    private Boolean encryptEnabled = true;

    /**
     * 加密并发线程池是否开启，默认不开启
     */
    @Setter
    private Boolean encryptExecutorEnabled = false;
    /**
     * 加密并发线程数，建议core=max
     */
    @Setter
    private Integer encryptCorePoolSize;
    /**
     * 加密并发线程数，建议core=max
     */
    @Setter
    private Integer encryptMaxPoolSize;

    /**
     * 解密是否开启，默认开启
     */
    @Setter
    private Boolean decryptEnabled = true;

    /**
     * 解密并发线程池是否开启，默认不开启
     */
    @Setter
    private Boolean decryptExecutorEnabled = false;
    /**
     * 解密并发线程数，建议core=max
     */
    @Setter
    private Integer decryptCorePoolSize;
    /**
     * 解密并发线程数，建议core=max
     */
    @Setter
    private Integer decryptMaxPoolSize;

    /**
     * 实际加解密处理器，需要自行实现
     */
    @Setter
    private SecurityHandler securityHandler;

    @Setter
    private String dbType = JdbcConstants.MYSQL;
    @Setter
    private Charset charset = StandardCharsets.UTF_8;

    private Map<String, Map<String, Map<String, Set<String>>>> encryptTableColumnsMap;
    private Map<String, Map<String, Map<String, Set<String>>>> decryptTableColumnsMap;

    // 加解密异步线程池
    private ExecutorService encryptExecutors;
    private ExecutorService decryptExecutors;

    public void setEncryptTableColumns(String encryptTableColumns) {
        encryptTableColumnsMap = parseTableColumns(encryptTableColumns);
    }

    public void setDecryptTableColumns(String decryptTableColumns) {
        decryptTableColumnsMap = parseTableColumns(decryptTableColumns);
    }

    // TODO union查询不支持（没法找到表名）
    protected void resultSetOpenAfter(ResultSetProxy resultSet) {
        if (!decryptEnabled) {
            // 未开启解密
            return;
        }

        ResultsetRows rows = ((ResultSetImpl) resultSet.getRawObject()).getRows();// 结果集
        Field[] fields = rows.getMetadata().getFields(); // 结果集字段描述
        List<Future> futureList = new LinkedList<>();
        for (int fieldIndex = 0; fieldIndex < fields.length; fieldIndex++) {
            final Field field = fields[fieldIndex];
            Map<String, Map<String, Set<String>>> decryptColumns = decryptTableColumnsMap.get(field.getOriginalTableName());
            if (decryptColumns == null) {
                // 不需要加密的表
                continue;
            }

            // 需要加密的字段
            if (decryptColumns.containsKey(field.getOriginalName())) {
                Map<String, Set<String>> conditionColumns = decryptColumns.get(field.getOriginalName());

                // 有条件字段
                if (conditionColumns != null) {
                    // 根据条件字段判断出的需要解密的行索引
                    Set<Row> requireDecryptRows = new LinkedHashSet<>();

                    // 循环根据条件字段和值找出需要解密的行
                    for (Map.Entry<String, Set<String>> conditionColumn : conditionColumns.entrySet()) {
                        for (int conditionFieldIndex = 0; conditionFieldIndex < fields.length; conditionFieldIndex++) {
                            if (conditionColumn.getKey().equals(fields[conditionFieldIndex].getOriginalName())) {
                                for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                                    Row row = rows.get(rowIndex);
                                    byte[] bytes = row.getBytes(conditionFieldIndex);
                                    if (bytes != null && bytes.length > 0) {
                                        String origValue = StringUtils.toString(bytes, charset.name());
                                        if (conditionColumn.getValue().contains(origValue)) {
                                            requireDecryptRows.add(row);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 遍历需要解密的行进行解密
                    for (final Row row : requireDecryptRows) {
                        decrypt(futureList, field.getOriginalTableName(), field.getOriginalName(), row, fieldIndex);
                    }

                }
                // 无条件字段的解密
                else {
                    // 遍历所有的行进行解密
                    for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                        final Row row = rows.get(rowIndex);
                        decrypt(futureList, field.getOriginalTableName(), field.getOriginalName(), row, fieldIndex);
                    }
                }
            }
        }

        for (Future future : futureList) {
            try {
                future.get();
            } catch (Exception e) {
                // 可能有部分解密成功，异常部分未解密
                log.warn("解密出现异常，异常部分未解密", e);
            }
        }
    }

    protected void statementExecuteBefore(StatementProxy statement, String sql) {
        if (!encryptEnabled) {
            // 未开启加密
            return;
        }

        // 使用druid API解析语句
        List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, this.dbType);
        Statement rawObject = statement.getRawObject();
        if (!(rawObject instanceof ClientPreparedStatement)) {
            log.trace("不需要处理的statement:{}", rawObject);
            return;
        }
        BindValue[] bindValues = ((ClientPreparedStatement) rawObject).getQueryBindings().getBindValues();

        // 解析出语句，通常只有一条，不支持超过一条语句的SQL
        for (SQLStatement stmt : stmtList) {
            MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
            stmt.accept(visitor);

            List<Future> futureList = new LinkedList<>();
            int index = 0;

            // 查询语句或删除语句，只有查询条件需要加密
            if (stmt instanceof SQLSelectStatement || stmt instanceof MySqlDeleteStatement) {
                // 遍历查询条件
                for (TableStat.Condition condition : visitor.getConditions()) {
                    // 遍历查询条件值，一般只有一个，但in/between语句等可能有多个
                    for (Object conditionValue : condition.getValues()) {
                        if (conditionValue == null) {// 解析出条件值为空才是查询条件
                            TableStat.Column column = condition.getColumn();
                            Map<String, Map<String, Set<String>>> encryptColumns = encryptTableColumnsMap.get(column.getTable());
                            if (encryptColumns == null) {
                                // 不需要加密的表
                                continue;
                            }

                            // 需要加密的字段
                            if (encryptColumns.containsKey(column.getName())
                                    && requireEncrypt(encryptColumns.get(column.getName()), bindValues, visitor)) {
                                encrypt(futureList, column.getTable(), column.getName(), bindValues[index]);
                            }
                            index++;
                        }
                    }
                }
            }
            // 插入语句
            else if (stmt instanceof MySqlInsertStatement) {
                MySqlInsertStatement insertStmt = (MySqlInsertStatement) stmt;
                // 插入语句应该只有一个表
                String tableName = insertStmt.getTableName().getSimpleName();
                Map<String, Map<String, Set<String>>> encryptColumns = encryptTableColumnsMap.get(tableName);
                if (encryptColumns == null) {
                    // 不需要加密的表
                    continue;
                }

                // valuesSize>1为batch insert语句
                int valuesSize = insertStmt.getValuesList().size();
                Collection<TableStat.Column> columns = visitor.getColumns();
                // 字段数量
                int columnSize = columns.size();
                for (TableStat.Column column : columns) {
                    // 需要加密的字段
                    if (encryptColumns.containsKey(column.getName())) {
                        Map<String, Set<String>> conditionColumns = encryptColumns.get(column.getName());
                        // 非条件型加密字段
                        if (conditionColumns == null) {
                            for (int valueIndex = 0; valueIndex < valuesSize; valueIndex++) {
                                BindValue bindValue = bindValues[index + valueIndex * columnSize];
                                encrypt(futureList, column.getTable(), column.getName(), bindValue);
                            }
                        }
                        // 条件型加密字段
                        else {
                            // 根据条件字段和字段值找出需要加密的BindValue
                            Set<BindValue> requireEncryptBindValues = new LinkedHashSet<>();
                            for (Map.Entry<String, Set<String>> conditionColumn : conditionColumns.entrySet()) {
                                int i = 0;
                                for (TableStat.Column column1 : columns) {
                                    if (column1.getName().equals(conditionColumn.getKey())) {
                                        for (int valueIndex = 0; valueIndex < valuesSize; valueIndex++) {
                                            BindValue bindValue = bindValues[i + valueIndex * columnSize];
                                            if (conditionColumn.getValue().contains(getBindValue(bindValue))) {
                                                requireEncryptBindValues.add(bindValues[index + valueIndex * columnSize]);
                                            }
                                        }
                                    }
                                    i++;
                                }
                            }

                            // 遍历找出的BindValue进行加密
                            for (BindValue bindValue : requireEncryptBindValues) {
                                encrypt(futureList, tableName, tableName, bindValue);
                            }
                        }
                    }
                    index++;
                }
            } else if (stmt instanceof MySqlUpdateStatement) {
                MySqlUpdateStatement updateStat = (MySqlUpdateStatement) stmt;
                // 更新语句应该只有一个表
                String tableName = updateStat.getTableName().getSimpleName();
                Map<String, Map<String, Set<String>>> encryptColumns = encryptTableColumnsMap.get(tableName);
                if (encryptColumns == null) {
                    // 不需要加密的表
                    continue;
                }

                // 先处理set语句
                for (SQLUpdateSetItem item : updateStat.getItems()) {
                    SQLExpr column = item.getColumn();
                    if (item.getValue() instanceof SQLVariantRefExpr && column instanceof SQLIdentifierExpr) {
                        String columnName = ((SQLIdentifierExpr) column).getName();
                        // 需要加密的字段
                        if (encryptColumns.containsKey(columnName)
                                && requireEncrypt(updateStat, encryptColumns.get(columnName), bindValues, visitor)) {
                            encrypt(futureList, tableName, columnName, bindValues[index]);
                        }
                        index++;
                    }
                }

                // 再处理where语句
                for (TableStat.Condition condition : visitor.getConditions()) {
                    // 遍历查询条件值，一般只有一个，但in/between语句等可能有多个
                    for (Object conditionValue : condition.getValues()) {
                        if (conditionValue == null) {// 解析出条件值为空才是查询条件
                            TableStat.Column column = condition.getColumn();
                            // 需要加密的字段
                            if (encryptColumns.containsKey(column.getName())
                                    && requireEncrypt(updateStat, encryptColumns.get(column.getName()), bindValues, visitor)) {
                                encrypt(futureList, column.getTable(), column.getName(), bindValues[index]);
                            }
                            index++;
                        }
                    }
                }

            }
            // 其他，一般没有了
            else {
                for (TableStat.Column column : visitor.getColumns()) {
                    Map<String, Map<String, Set<String>>> encryptColumns = encryptTableColumnsMap.get(column.getTable());
                    if (encryptColumns == null) {
                        // 不需要加密的表
                        continue;
                    }

                    // 需要加密的字段
                    if (encryptColumns.containsKey(column.getName())) {
                        Map<String, Set<String>> conditionColumns = encryptColumns.get(column.getName());

                        boolean requireEncrypt = false;
                        if (conditionColumns == null) {
                            // 非条件字段需要加密
                            requireEncrypt = true;
                        } else {
                            // 条件字段需要先根据条件字段和条件字段值判断是否需要加密
                            for (Map.Entry<String, Set<String>> conditionColumn : conditionColumns.entrySet()) {
                                int i = 0;
                                for (TableStat.Column column1 : visitor.getColumns()) {
                                    BindValue bindValue = bindValues[i];
                                    if (column1.getName().equals(conditionColumn.getKey())) {
                                        if (conditionColumn.getValue().contains(getBindValue(bindValue))) {
                                            requireEncrypt = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }

                        if (requireEncrypt) {
                            encrypt(futureList, column.getTable(), column.getName(), bindValues[index]);
                        }
                    }
                    index++;
                }
            }

            for (Future future : futureList) {
                try {
                    future.get();
                } catch (Exception e) {
                    // 可能有部分加密成功，异常部分未加密
                    log.warn("加密出现异常，异常部分未加密", e);
                }
            }
        }
    }


    private boolean requireEncrypt(MySqlUpdateStatement updateStat, Map<String, Set<String>> conditionColumns, BindValue[] bindValues, MySqlSchemaStatVisitor visitor) {
        boolean requireEncrypt = false;
        // 非条件型加密字段
        if (conditionColumns == null) {
            requireEncrypt = true;
        }
        // 条件型加密字段
        else {
            int bindIndex = 0;
            // 先判断SET语句
            for (SQLUpdateSetItem item : updateStat.getItems()) {
                if ((item.getValue() instanceof SQLVariantRefExpr || item.getValue() instanceof SQLValuableExpr) && item.getColumn() instanceof SQLIdentifierExpr) {
                    String columnName = ((SQLIdentifierExpr) item.getColumn()).getName();
                    for (Map.Entry<String, Set<String>> conditionColumn : conditionColumns.entrySet()) {
                        if (conditionColumn.getKey().equals(columnName)) {
                            // 判断绑定值是否配置，如：where param_name=?
                            if (item.getValue() instanceof SQLVariantRefExpr
                                    && conditionColumn.getValue().contains(getBindValue(bindValues[((SQLVariantRefExpr) item.getValue()).getIndex()]))) {
                                requireEncrypt = true;
                                break;
                            }
                            // 为固定值数值类型，转成文本进行判断, 如：where user_id=1
                            else if (item.getValue() instanceof SQLIntegerExpr
                                    && conditionColumn.getValue().contains(String.valueOf(((SQLIntegerExpr) item.getValue()).getValue()))) {
                                requireEncrypt = true;
                                break;
                            }
                            // 为固定值文本类型，直接进行判断，如：where type='encrypt'
                            else if (item.getValue() instanceof SQLCharExpr
                                    && conditionColumn.getValue().contains(((SQLCharExpr) item.getValue()).getText())) {
                                requireEncrypt = true;
                                break;
                            }
                        }
                    }

                    bindIndex++;
                }
                if (requireEncrypt) {
                    break;
                }
            }

            // 再判断查询条件
            if (!requireEncrypt) {
                for (TableStat.Condition condition : visitor.getConditions()) {
                    // 遍历查询条件值，一般只有一个，但in/between语句等可能有多个
                    for (Object conditionValue : condition.getValues()) {
                        if (conditionValue == null) {// 解析出条件值为空才是查询条件
                            TableStat.Column column = condition.getColumn();
                            // 判断查询条件字段名和绑定值是否与配置相同
                            for (Map.Entry<String, Set<String>> conditionColumn : conditionColumns.entrySet()) {
                                if (conditionColumn.getKey().equals(column.getName())
                                        && conditionColumn.getValue().contains(getBindValue(bindValues[bindIndex]))) {
                                    requireEncrypt = true;
                                    break;
                                }
                            }

                            bindIndex++;
                        } else {
                            TableStat.Column column = condition.getColumn();
                            // 判断查询条件字段名和固定值是否与配置相同，如 where param_name='phone'
                            for (Map.Entry<String, Set<String>> conditionColumn : conditionColumns.entrySet()) {
                                if (conditionColumn.getKey().equals(column.getName())
                                        && conditionColumn.getValue().contains(String.valueOf(conditionValue))) {
                                    requireEncrypt = true;
                                    break;
                                }
                            }
                        }

                        if (requireEncrypt) {
                            break;
                        }
                    }
                    if (requireEncrypt) {
                        break;
                    }
                }
            }
        }

        return requireEncrypt;
    }

    private boolean requireEncrypt(Map<String, Set<String>> conditionColumns, BindValue[] bindValues, MySqlSchemaStatVisitor visitor) {
        boolean requireEncrypt = false;
        // 非条件型加密字段
        if (conditionColumns == null) {
            requireEncrypt = true;
        }
        // 条件型加密字段
        else {
            int bindValueIndex = 0;
            for (TableStat.Condition condition : visitor.getConditions()) {
                // 遍历查询条件值，一般只有一个，但in/between语句等可能有多个
                for (Object conditionValue : condition.getValues()) {
                    if (conditionValue == null) {
                        // 判断查询条件字段名和绑定值是否与配置相同
                        for (Map.Entry<String, Set<String>> conditionColumn : conditionColumns.entrySet()) {
                            if (conditionColumn.getKey().equals(condition.getColumn().getName())
                                    && conditionColumn.getValue().contains(getBindValue(bindValues[bindValueIndex]))) {
                                requireEncrypt = true;
                                break;
                            }
                        }
                        bindValueIndex++;
                    } else {
                        // 判断查询条件字段名和固定值是否与配置相同，如 where param_name='phone'
                        for (Map.Entry<String, Set<String>> conditionColumn : conditionColumns.entrySet()) {
                            if (conditionColumn.getKey().equals(condition.getColumn().getName())
                                    && conditionColumn.getValue().contains(String.valueOf(conditionValue))) {
                                requireEncrypt = true;
                                break;
                            }
                        }
                    }

                    if (requireEncrypt) {
                        break;
                    }
                }
                if (requireEncrypt) {
                    break;
                }
            }
        }

        return requireEncrypt;
    }

    private void encrypt(List<Future> futureList, final String tableName, final String columnName, final BindValue bindValue) {
        final String origValue = getBindValue(bindValue);
        if (origValue == null) {
            return;
        }

        if (encryptExecutorEnabled) {
            Future future = encryptExecutors.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    encrypt(tableName, columnName, origValue, bindValue);
                    return null;
                }
            });
            futureList.add(future);
        } else {
            encrypt(tableName, columnName, origValue, bindValue);
        }
    }

    private void encrypt(String tableName, String columnName, String origValue, BindValue bindValue) {
        String encryptValue = securityHandler.encrypt(tableName, columnName, origValue);
        log.trace("字段加密：{}.{}={}->{}", tableName, columnName, origValue, encryptValue);
        encryptValue = "'" + encryptValue + "'";
        bindValue.setByteValue(encryptValue.getBytes(charset));
    }

    private void decrypt(List<Future> futureList, final String tableName, final String columnName, final Row row, final int index) {
        byte[] bytes = row.getBytes(index);
        if (bytes != null && bytes.length > 0) {
            final String origValue = StringUtils.toString(bytes, charset.name());
            if (decryptExecutorEnabled) {
                Future<String> future = decryptExecutors.submit(new Callable<String>() {
                    @Override
                    public String call() {
                        decrypt(tableName, columnName, row, origValue, index);
                        return null;
                    }
                });
                futureList.add(future);
            } else {
                decrypt(tableName, columnName, row, origValue, index);
            }
        }
    }

    private void decrypt(String tableName, String columnName, Row row, String origValue, int index) {
        String decryptValue = securityHandler.decrypt(tableName, columnName, origValue);
        log.trace("字段解密：{}.{}={}->{}", tableName, columnName, origValue, decryptValue);
        row.setBytes(index, decryptValue.getBytes(charset));
    }

    private String getBindValue(BindValue bindValue) {
        if (bindValue.isNull()) {
            return null;
        }
        byte[] byteValue = bindValue.getByteValue();
        if (byteValue == null || byteValue.length == 0) {
            return null;
        }
        String origValue = StringUtils.toString(byteValue, charset.name());
        if ("''".equals(origValue) || "".equals(origValue)) {
            return null;
        }
        // 参数可能自带''单引号，需要去掉''单引号
        if (origValue.startsWith("'") && origValue.endsWith("'")) {
            origValue = origValue.substring(1, origValue.length() - 1);
        }

        return origValue;
    }

    private Map<String, Map<String, Map<String, Set<String>>>> parseTableColumns(String value) {
        Map<String, Map<String, Map<String, Set<String>>>> map = new HashMap<>();
        if (value == null || value.trim().length() == 0) {
            return null;
        }
        value = value.trim();

        String[] array = value.split(SymbolConstants.SEPARATOR_FEN);
        for (String str : array) {
            String[] arr = str.split(SymbolConstants.EQUAL);
            if (arr.length != 2) {
                throw new TableColumnException("[" + value + "] 属性设置出错]");
            }
            //获取到表名
            String tableName = arr[0];
            String columnStr = arr[1];
            String[] columnArr = columnStr.split(SymbolConstants.SEPARATOR);
            //获取到列
            Map<String, Map<String, Set<String>>> columns = new HashMap();
            for (String column : columnArr) {
                // 格式形如：param_value[param_name=certNo|param_name=phone]
                if (column.matches("\\w+\\[\\w+:.+\\]")) {
                    // 条件型加解密参数
                    String realColumn = column.substring(0, column.indexOf("["));
                    String columnCondition = column.substring(column.indexOf("[") + 1, column.length() - 1);
                    String[] columnConditionArr = columnCondition.split("\\|");
                    Map<String, Set<String>> columnConditionMap = new HashMap<>();
                    for (String condition : columnConditionArr) {
                        String[] conditionColumn = condition.split(":");
                        Set<String> conditionValues = columnConditionMap.get(conditionColumn[0]);
                        if (conditionValues == null) {
                            conditionValues = new HashSet<>();
                            columnConditionMap.put(conditionColumn[0], conditionValues);
                        }
                        conditionValues.add(conditionColumn[1]);
                    }
                    columns.put(realColumn, columnConditionMap);
                } else {
                    columns.put(column, null);
                }
                map.put(tableName, columns);
            }
        }
        return map;
    }

    public void init() {
        if (encryptEnabled || decryptEnabled) {
            if (securityHandler == null) {
                log.error("securityHandler不能为空");
                throw new DBEncryptException("securityHandler不能为空");
            }
        }

        if (encryptEnabled) {
            if (encryptTableColumnsMap == null || encryptTableColumnsMap.isEmpty()) {
                log.error("encryptTableColumns配置为空或有误");
                throw new DBEncryptException("encryptTableColumns配置为空或有误");
            }

            if (this.encryptExecutorEnabled) {
                if (encryptCorePoolSize == null || encryptCorePoolSize <= 0) {
                    log.error("encryptCorePoolSize配置为空或有误");
                    throw new DBEncryptException("encryptCorePoolSize配置为空或有误");
                }
                if (encryptMaxPoolSize == null || encryptMaxPoolSize <= 0) {
                    log.error("encryptMaxPoolSize配置为空或有误");
                    throw new DBEncryptException("encryptMaxPoolSize配置为空或有误");
                }
                log.trace("初始化加密线程池：corePoolSize={}, maxPoolSize={}", this.encryptCorePoolSize, this.encryptMaxPoolSize);
                this.encryptExecutors = new ThreadPoolExecutor(
                        this.encryptCorePoolSize,
                        this.encryptMaxPoolSize,
                        300,
                        TimeUnit.SECONDS,
                        new SynchronousQueue<Runnable>(), // 队列大小为零，超过线程池数量后直接在当前线程中
                        new NamedThreadFactory("encrypt-"),
                        new ThreadPoolExecutor.CallerRunsPolicy() // 超过线程池数量后直接在当前线程中
                );
            }
            log.info("数据库字段加密已启用：{}", encryptTableColumnsMap);
        }

        if (decryptEnabled) {
            if (decryptTableColumnsMap == null || decryptTableColumnsMap.isEmpty()) {
                log.error("decryptTableColumns配置为空或有误");
                throw new DBEncryptException("decryptTableColumns配置为空或有误");
            }

            if (this.decryptExecutorEnabled) {
                if (decryptCorePoolSize == null || decryptCorePoolSize <= 0) {
                    log.error("decryptCorePoolSize配置为空或有误");
                    throw new DBEncryptException("decryptCorePoolSize配置为空或有误");
                }
                if (decryptMaxPoolSize == null || decryptMaxPoolSize <= 0) {
                    log.error("decryptMaxPoolSize配置为空或有误");
                    throw new DBEncryptException("decryptMaxPoolSize配置为空或有误");
                }
                log.trace("初始化解密线程池：corePoolSize={}, maxPoolSize={}", this.decryptCorePoolSize, this.decryptMaxPoolSize);
                this.decryptExecutors = new ThreadPoolExecutor(
                        this.decryptCorePoolSize,
                        this.decryptMaxPoolSize,
                        300,
                        TimeUnit.SECONDS,
                        new SynchronousQueue<Runnable>(),// 队列大小为零，超过线程池数量后直接在当前线程中
                        new NamedThreadFactory("decrypt-"),
                        new ThreadPoolExecutor.CallerRunsPolicy() // 超过线程池数量后直接在当前线程中
                );
            }
            log.info("数据库字段解密已启用：{}", encryptTableColumnsMap);
        }

    }

    static class NamedThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        NamedThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        public Thread newThread(Runnable r) {
            return new Thread(r, namePrefix + threadNumber.getAndIncrement());

        }
    }
}
