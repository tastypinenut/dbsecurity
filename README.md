# dbsecurity
数据库字段加解密插件，可定制加密的表，字段等，具体实现算法需要实现SecurityHandler接口自行实现。

本插件基于druid(1.1.10)和mysql(8.0.x)实现，原理为通过druid中的工具类解析出字段和条件的位置，通过mysql驱动中Statement和ResultSet实现类获取参数值或结果值进行加解密。新版本druid可能不兼容，需谨慎升级druid版本。

## 如何使用
    
### Spring Boot项目

1、增加依赖

```java
<dependency>
    <groupId>xyz.tpn.springboot</groupId>
    <artifactId>dbsecurity-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

2、实现SecurityHandler

3、增加配置
```properties
# 加密是否启用
dbsecurity.encrypt.enabled=true
# 需要加密的表名字段名
dbsecurity.encrypt.table-columns=t_user_info=name,phone,bank_card_no;t_param=param_value[param_name:name|param_name:phone]
# 是否启用并发线程池，默认不启用
dbsecurity.encrypt.executor-enabled=true
# 并发线程数，需要根据具体业务量以及机器性能合理评估
dbsecurity.encrypt.core-pool-size=10
dbsecurity.encrypt.max-pool-size=100

# 解密是否启用
dbsecurity.decrypt.enabled=true
# 需要解密的表名字段名
dbsecurity.decrypt.table-columns=t_user_info=name,phone,bank_card_no;t_param=param_value[param_name:name|param_name:phone]
# 是否启用并发线程池，默认不启用
dbsecurity.decrypt.executor-enabled=true
# 并发线程数，需要根据具体业务量以及机器性能合理评估
dbsecurity.decrypt.core-pool-size=10
dbsecurity.decrypt.max-pool-size=100
```
    
### Spring 项目
1、增加依赖

```java
<dependency>
    <groupId>xyz.tpn</groupId>
    <artifactId>dbsecurity</artifactId>
    <version>1.0.0</version>
</dependency>
```

2、实现SecurityHandler

3、增加配置
```xml
    <!-- 数据源 -->
    <bean id="dataSource" class="com.alibaba.druid.pool.DruidDataSource"
          init-method="init" destroy-method="close">
        <!-- 数据源其他配置 -->
        <!-- ... -->

        <property name="proxyFilters">
            <list>
                <!-- 配置加解密拦截器 -->
                <ref bean="securityFilter"/>
            </list>
        </property>
    </bean>

    <!-- 加解密拦截器 -->
    <bean id="securityFilter" class="xyz.tpn.dbsecurity.druid.SecurityFilter" init-method="init">
        <!-- 加密是否启用 -->
        <property name="encryptEnabled" value="true"/>
        <!-- 需要加密的表名字段名 -->
        <property name="encryptTableColumns" value="t_user_info=name,phone,bank_card_no;t_param=param_value[param_name:name|param_name:phone]/>
        <!-- 是否启用并发线程池，默认不启用 -->
        <property name="encryptExecutorEnabled" value="true"/>
        <!-- 并发线程数，需要根据具体业务量以及机器性能合理评估 -->
        <property name="encryptCorePoolSize" value="10"/>
        <property name="encryptMaxPoolSize" value="100"/>

        <!-- 解密是否启用 -->
        <property name="decryptEnabled" value="true"/>
        <!-- 需要解密的表名字段名 -->
        <property name="decryptTableColumns" value="t_user_info=name,phone,bank_card_no;t_param=param_value[param_name:name|param_name:phone]/>
        <!-- 是否启用并发线程池，默认不启用-->
        <property name="decryptExecutorEnabled" value="true"/>
        <!-- 并发线程数，需要根据具体业务量以及机器性能合理评估 -->
        <property name="decryptCorePoolSize" value="10"/>
        <property name="decryptMaxPoolSize" value="100"/>

        <property name="securityHandler" ref="securityHandler"/>
    </bean>

    <!-- 自行实现SecurityHandler-->
    <bean id="securityHandler" class="xxx.yyy.SecurityHandler">
    </bean>
```

## 注意事项  

请仔细阅读后文档后使用
    
## 问题

    1. 不支持union语句查询结果解密
    原因：union语句查询结果可能来自多个表，本插件无法根据表名、字段名确定是否需要解密

    2. 垂直表已支持，但需要注意一些问题
        配置格式：表名=加解密字段名[条件字段名:条件字段值|其他条件字段名:其他条件字段值]，当同一行中条件字段值相同时，才会对字段加解密，以此实现某些行中的字段需要加解密，某些行不加解密
        如：t_param=param_value[param_name:realName|param_name:phone]
        问题：
            1、条件字段和加解密字段必须同时出现在查询条件或查询结果集中，否则无法判断是否需要加解密：
                正例：update t_param set param_value=?, param_name=? where id=?;
                反例：update t_param set param_value=? where id=?; （param_name不存在，无法判断param_value是否需要加密）
                反例：select * from t_param wehre param_value=?; （param_name不存在，param_value不会加密后查询）
                反例：select param_value from t_param; (查询结果中没有param_name, 无法判断此行是否需要解密）
            2、不支持OR、IN等复杂不确定查询条件，否则会错误的加解密
                反例：update t_param set param_value=? where id=? or param_name=?; （OR条件会引起不必要的加密）
                反例：select * from t_param wehre param_name in (?,?) and param_value=?; （IN语句会引起不必要的加密）
            3、多个条件字段并不是与判断（只支持或判断），如配置为：t_param=param_value[param_name:phone|param_name2:phone]
                例：insert into(param_name,param_name2,param_value) values('phone', 'phone', '13212345678'); -- 加密
                例：insert into(param_name,param_name2,param_value) values('phone', 'not_phone', '13212345678'); -- 加密
                例：insert into(param_name,param_name2,param_value) values('not_phone', 'phone', '13212345678'); -- 加密
                例：insert into(param_name,param_name2,param_value) values('not_phone', 'not_phone', '13212345678'); -- 不加密
    
    3. 加密字段不支持模糊查询
    
    4. 仅支持druid(1.1.10), mysql(8.0.x)以上，升级版本后请注意回归测试


## 版本历史

### version 1.0.0