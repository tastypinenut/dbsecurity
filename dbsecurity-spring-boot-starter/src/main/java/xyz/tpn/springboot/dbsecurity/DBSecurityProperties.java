package xyz.tpn.springboot.dbsecurity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = DBSecurityProperties.PREFIX)
public class DBSecurityProperties {

    public static final String PREFIX = "dbsecurity";

    /**
     * 数据库类型，默认mysql，其他类型详见JdbcConstants
     */
    private String dbType = "mysql";
    /**
     * 字符编码类型，默认utf-8
     */
    private String charset = "utf-8";
    /**
     * 加密配置
     */
    private Config encrypt = new Config();
    /**
     * 解密配置
     */
    private Config decrypt = new Config();

    @Getter
    @Setter
    public static class Config {
        /**
         * 是否启用
         */
        private Boolean enabled = true;
        /**
         * 需要加解密的表名字段名，格式如：t_user_info=name,phone,bank_card_no;xx=yy
         */
        private String tableColumns;

        /**
         * 是否启用并发线程池，默认不启用
         */
        private Boolean executorEnabled = false;
        /**
         * 并发线程数，需要根据具体业务量以及机器性能合理评估，建议core=max
         */
        private Integer corePoolSize;
        /**
         * 并发线程数，需要根据具体业务量以及机器性能合理评估，建议core=max
         */
        private Integer maxPoolSize;
    }
}
