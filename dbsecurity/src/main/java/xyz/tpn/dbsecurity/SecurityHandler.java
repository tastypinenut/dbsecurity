package xyz.tpn.dbsecurity;

public interface SecurityHandler {

    /**
     * 加密
     *
     * @param tableName  表名
     * @param columnName 字段名
     * @param parameter  参数
     * @return 加密后字符串
     */
    String encrypt(String tableName, String columnName, String parameter);

    /**
     * 解密
     *
     * @param tableName  表名
     * @param columnName 字段名
     * @param parameter  参数
     * @return 解密后字符串
     */
    String decrypt(String tableName, String columnName, String parameter);
}
