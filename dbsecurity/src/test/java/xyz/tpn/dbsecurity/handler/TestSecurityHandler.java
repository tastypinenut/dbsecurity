package xyz.tpn.dbsecurity.handler;

import xyz.tpn.dbsecurity.SecurityHandler;
import xyz.tpn.dbsecurity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestSecurityHandler implements SecurityHandler {
    @Override
    public String encrypt(String tableName, String columnName, String parameter) {
        String encryptedParameter = SecurityUtils.encrypt(parameter);
        log.info("{}:encrypted: {}->{}", Thread.currentThread().getName(), parameter, encryptedParameter);
        return encryptedParameter;
    }

    @Override
    public String decrypt(String tableName, String columnName, String parameter) {
        String decryptedParameter = SecurityUtils.decrypt(parameter);
        log.info("{}:decrypted: {}->{}", Thread.currentThread().getName(), parameter, decryptedParameter);
        return decryptedParameter;
    }
}
