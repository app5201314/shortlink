package com.lcx.shortlink.gateway.toolkit;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyUtils {

    public static ECPublicKey readPublicKeyFromFile(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return (ECPublicKey) keyFactory.generatePublic(spec);
    }

    public static ECPrivateKey readPrivateKeyFromFile(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return (ECPrivateKey) keyFactory.generatePrivate(spec);
    }

    public static void generateKeys() throws Exception {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
            keyGen.initialize(256);
            KeyPair keyPair = keyGen.generateKeyPair();
            ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();
            ECPrivateKey privateKey = (ECPrivateKey) keyPair.getPrivate();


            // 将公钥和私钥写入D:\桌面\java_project\shortlink\admin\src\main\resources\keys\publicKey和privateKey文件中
            Files.write(Paths.get("D:\\桌面\\java_project\\shortlink\\admin\\src\\main\\resources\\keys\\publicKey"), publicKey.getEncoded());
            Files.write(Paths.get("D:\\桌面\\java_project\\shortlink\\admin\\src\\main\\resources\\keys\\privateKey"), privateKey.getEncoded());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // 读取公钥和私钥
        try {
            ECPublicKey publicKey = readPublicKeyFromFile("D:\\桌面\\java_project\\shortlink\\admin\\src\\main\\resources\\keys\\publicKey");
            ECPrivateKey privateKey = readPrivateKeyFromFile("D:\\桌面\\java_project\\shortlink\\admin\\src\\main\\resources\\keys\\privateKey");

            System.out.println(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
            System.out.println(Base64.getEncoder().encodeToString(privateKey.getEncoded()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
