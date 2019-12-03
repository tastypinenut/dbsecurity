package xyz.tpn.springboot.dbsecurity;

import xyz.tpn.dbsecurity.SecurityHandler;
import xyz.tpn.dbsecurity.druid.SecurityFilter;
import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.Charset;
import java.util.Arrays;


@Configuration
@EnableConfigurationProperties(DBSecurityProperties.class)
public class DBSecurityAutoConfiguration {

    @Autowired
    private DBSecurityProperties properties;

    @Bean("securityFilter")
    public SecurityFilter securityFilter(SecurityHandler securityHandler) {
        SecurityFilter securityFilter = new SecurityFilter();

        securityFilter.setEncryptEnabled(properties.getEncrypt().getEnabled());
        securityFilter.setEncryptTableColumns(properties.getEncrypt().getTableColumns());
        securityFilter.setEncryptExecutorEnabled(properties.getEncrypt().getExecutorEnabled());
        securityFilter.setEncryptCorePoolSize(properties.getEncrypt().getCorePoolSize());
        securityFilter.setEncryptMaxPoolSize(properties.getEncrypt().getMaxPoolSize());

        securityFilter.setDecryptEnabled(properties.getDecrypt().getEnabled());
        securityFilter.setDecryptTableColumns(properties.getDecrypt().getTableColumns());
        securityFilter.setDecryptExecutorEnabled(properties.getDecrypt().getExecutorEnabled());
        securityFilter.setDecryptCorePoolSize(properties.getDecrypt().getCorePoolSize());
        securityFilter.setDecryptMaxPoolSize(properties.getDecrypt().getMaxPoolSize());

        securityFilter.setSecurityHandler(securityHandler);

        securityFilter.setDbType(properties.getDbType());
        securityFilter.setCharset(Charset.forName(properties.getCharset()));
        securityFilter.init();
        return securityFilter;
    }

    @Autowired
    public void setProxyFilter(DruidDataSource druidDataSource, SecurityFilter securityFilter){
        druidDataSource.setProxyFilters(Arrays.<Filter>asList(securityFilter));
    }
}
