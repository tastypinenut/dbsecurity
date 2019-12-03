package xyz.tpn.dbsecurity.exception;

public class DBEncryptException extends RuntimeException {
    public DBEncryptException(String message) {
        super(message);
    }

    public DBEncryptException(Throwable e) {
        super(e);
    }

}
