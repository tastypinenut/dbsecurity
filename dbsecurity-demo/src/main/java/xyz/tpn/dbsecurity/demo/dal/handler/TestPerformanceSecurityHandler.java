package xyz.tpn.dbsecurity.demo.dal.handler;

import java.util.concurrent.TimeUnit;

public class TestPerformanceSecurityHandler extends TestSecurityHandler {
    @Override
    public String encrypt(String tableName, String columnName, String parameter) {
        sleep(10);
        return super.encrypt(tableName, columnName, parameter);
    }

    @Override
    public String decrypt(String tableName, String columnName, String parameter) {
        sleep(10);
        return super.decrypt(tableName, columnName, parameter);
    }

    private void sleep(long milliSeconds) {
        try {
            TimeUnit.MILLISECONDS.sleep(milliSeconds);
        } catch (InterruptedException e) {
            // do nothing
        }
    }
}
