package com.lcx.shortlink.admin.test;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.ECDSAKeyProvider;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Date;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class JwtTools {

    private static ECDSAKeyProvider keyProvider;

    static {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
            keyGen.initialize(256);
            KeyPair keyPair = keyGen.generateKeyPair();
            ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();
            ECPrivateKey privateKey = (ECPrivateKey) keyPair.getPrivate();

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
    public static String genToken(Map<String, Object> claims) {
        return JWT.create()
                .withClaim("claims", claims)
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .sign(Algorithm.ECDSA256(keyProvider));
    }

    //接收token,验证token,并返回业务数据
    public static Map<String, Object> parseToken(String token) {
        DecodedJWT jwt = JWT.require(Algorithm.ECDSA256(keyProvider))
                .build()
                .verify(token);
        return jwt.getClaim("claims").asMap();
    }
}

