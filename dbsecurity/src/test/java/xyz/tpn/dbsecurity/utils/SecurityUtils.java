package xyz.tpn.dbsecurity.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SecurityUtils {
    public static String encrypt(String parameter) {
        if (parameter.startsWith("###") || parameter.endsWith("###")) {
            throw new IllegalArgumentException("加密失败，原字段并已加密：" + parameter);
        } else {
            return "###" + parameter + "###";
        }
    }

    public static String decrypt(String parameter) {
        if (parameter.startsWith("###") && parameter.endsWith("###")) {
            return parameter.replaceAll("###", "");
        } else {
            throw new IllegalArgumentException("解密失败，原字段并未加密：" + parameter);
        }
    }

    public static boolean unEncryptedColumn(String value) {
        if (value.startsWith("###") || value.endsWith("###")) {
            log.info("[{}]应当是未加密字段", value);
            return false;
        }

        return true;
    }

    public static boolean isDecrypted(String value) {
        if (value.startsWith("###") || value.endsWith("###")) {
            log.info("[{}]应当被解密", value);
            return false;
        }

        return true;
    }

    public static boolean isEncrypted(String value) {
        if (value.startsWith("###") && value.endsWith("###")) {
            return true;
        }

        log.info("[{}]应当被加密", value);
        return false;
    }
}
