package com.lcx.shortlink.admin.toolkit;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class TripleDES {

    private static final String KEY_FILE_PATH = "D:\\桌面\\java_project\\shortlink\\admin\\src\\main\\resources\\3DES\\key";
    private static SecretKey key;
    private static Cipher cipher;

    static {
        try {
            byte[] keyBytes = Files.readAllBytes(Paths.get(KEY_FILE_PATH));
            key = new SecretKeySpec(keyBytes, "DESede");
            cipher = Cipher.getInstance("DESede");
        } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    public static void generateKey() throws NoSuchAlgorithmException, IOException {
        KeyGenerator keyGen = KeyGenerator.getInstance("DESede");
        SecretKey secretKey = keyGen.generateKey();
        byte[] key = secretKey.getEncoded();
        try (FileOutputStream fos = new FileOutputStream(KEY_FILE_PATH)) {
            fos.write(key);
        }
        TripleDES.key = new SecretKeySpec(key, "DESede");
    }

    public static String encrypt(String message) throws Exception {
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(messageBytes);
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decrypt(String encrypted) throws Exception {
        byte[] encryptedBytes = Base64.getDecoder().decode(encrypted);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}