package xyz.tpn.dbsecurity.druid;


import xyz.tpn.dbsecurity.handler.TestPerformanceSecurityHandler;
import xyz.tpn.dbsecurity.handler.TestSecurityHandler;
import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.datasource.DataSourceFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;

@Slf4j
public class DruidDataSourceFactory implements DataSourceFactory {
    private Properties props;

    @Override
    public DataSource getDataSource() {
        DruidDataSource dds = new DruidDataSource();
        // 其他配置可以根据MyBatis主配置文件进行配置
        try {

            dds.setDriverClassName(this.props.getProperty("jdbc.driver"));
            dds.setUrl(this.props.getProperty("jdbc.url"));
            dds.setUsername(this.props.getProperty("jdbc.user"));
            dds.setPassword(this.props.getProperty("jdbc.password"));

            SecurityFilter filter = new SecurityFilter();
            filter.setEncryptTableColumns(this.props.getProperty("encryptTableColumns"));
            filter.setDecryptTableColumns(this.props.getProperty("decryptTableColumns"));
            filter.setSecurityHandler(new TestSecurityHandler());

            // 异步线程池
//            filter.setEncryptExecutorEnabled(true);
//            filter.setEncryptCorePoolSize(10);
//            filter.setEncryptMaxPoolSize(100);
//            filter.setDecryptExecutorEnabled(true);
//            filter.setDecryptCorePoolSize(10);
//            filter.setDecryptMaxPoolSize(100);
//            filter.setSecurityHandler(new TestPerformanceSecurityHandler());

            filter.init();
            dds.setProxyFilters(Arrays.<Filter>asList(filter));

            dds.init();
        } catch (SQLException e) {
            log.error("", e);
        }
        return dds;
    }

    @Override
    public void setProperties(Properties props) {
        this.props = props;
    }
}
