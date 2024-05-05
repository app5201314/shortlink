package com.lcx.shortlink.admin.toolkit;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.ECDSAKeyProvider;

import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Date;
import java.util.Map;

public class JwtTools {

    private static ECDSAKeyProvider keyProvider;

    static {
        try {
            // 读取公钥和私钥
            ECPublicKey publicKey = KeyUtils.readPublicKeyFromFile("D:\\桌面\\java_project\\shortlink\\admin\\src\\main\\resources\\keys\\publicKey");
            ECPrivateKey privateKey = KeyUtils.readPrivateKeyFromFile("D:\\桌面\\java_project\\shortlink\\admin\\src\\main\\resources\\keys\\privateKey");

            keyProvider = new ECDSAKeyProvider() {
                @Override
                public ECPublicKey getPublicKeyById(String keyId) {
                    return publicKey;
                }

                @Override
                public ECPrivateKey getPrivateKey() {
                    return privateKey;
                }

                @Override
                public String getPrivateKeyId() {
                    return null; // 可以返回一个标识私钥的字符串，如果不需要可以返回null
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //接收业务数据,生成token并返回
    public static String genSignature(Map<String, Object> claims) {
        return JWT.create()
                .withClaim("claims", claims)
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 30))
                .sign(Algorithm.ECDSA256(keyProvider));
    }

    //接收token,验证token,并返回业务数据
    public static Map<String, Object> parseSignature(String token) {
        DecodedJWT jwt = JWT.require(Algorithm.ECDSA256(keyProvider))
                .build()
                .verify(token);
        return jwt.getClaim("claims").asMap();
    }
}




