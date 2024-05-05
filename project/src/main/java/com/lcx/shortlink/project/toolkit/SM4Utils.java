package com.lcx.shortlink.project.toolkit;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.security.Security;

public class SM4Utils {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final String ALGORITHM_NAME = "SM4";
    private static final String ALGORITHM_NAME_ECB_PADDING = "SM4/ECB/PKCS5Padding";
    private static final String SECRET_KEY;

    static {
        SECRET_KEY = readKeyFromFile("D:\\桌面\\java_project\\shortlink\\project\\src\\main\\resources\\keys\\sm4.key");
    }

    public static String encryptDataECB(String plainText) {
        return encryptDataECB(plainText, SECRET_KEY);
    }

    public static String decryptDataECB(String cipherText) {
        return decryptDataECB(cipherText, SECRET_KEY);
    }

    private static String encryptDataECB(String plainText, String secretKey) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_NAME_ECB_PADDING, BouncyCastleProvider.PROVIDER_NAME);
            Key sm4Key = new SecretKeySpec(Hex.decode(secretKey), ALGORITHM_NAME);
            cipher.init(Cipher.ENCRYPT_MODE, sm4Key);
            byte[] result = cipher.doFinal(plainText.getBytes());
            return ByteUtils.toHexString(result);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String decryptDataECB(String cipherText, String secretKey) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_NAME_ECB_PADDING, BouncyCastleProvider.PROVIDER_NAME);
            Key sm4Key = new SecretKeySpec(Hex.decode(secretKey), ALGORITHM_NAME);
            cipher.init(Cipher.DECRYPT_MODE, sm4Key);
            byte[] result = cipher.doFinal(Hex.decode(cipherText));
            return new String(result, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String readKeyFromFile(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        // 将密钥写入文件
//        String key = "0123456789abcdeffedcba9876543210";
//        try {
//            Files.write(Paths.get("D:\\桌面\\java_project\\shortlink\\project\\src\\main\\resources\\keys\\sm4.key"), key.getBytes());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        String plainText = "123456";
        String cipherText = encryptDataECB(plainText);
        System.out.println("密文：" + cipherText);
        System.out.println("明文：" + decryptDataECB(cipherText));
    }
}