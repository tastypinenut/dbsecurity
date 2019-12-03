package xyz.tpn.dbsecurity.demo.dal.handler;

import xyz.tpn.dbsecurity.SecurityHandler;
import xyz.tpn.dbsecurity.demo.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
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
