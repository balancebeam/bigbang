package io.anyway.bigbang.framework.utils;



import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;


public interface RSAUtil {

    Logger log= LoggerFactory.getLogger(RSAUtil.class);

    static KeyPair generateKey() throws NoSuchAlgorithmException {
        //初始化密码对生成器（"RSA"，密钥算法标识）
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        //初始化密码对生成器（"1024", 密钥长度, 还支持2048，4096长度，第二个参数是个随机数）
        keyPairGen.initialize(2048, new SecureRandom());
        //使用密码对生成器，产生一对密钥
        KeyPair keyPair = keyPairGen.generateKeyPair();
        return keyPair;
    }

//    static KeyPair generateKey(String pem) throws NoSuchAlgorithmException {
//        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
//        keyPairGen.initialize(2048, new SecureRandom());
//        KeyPair keyPair = keyPairGen.generateKeyPair();
//        return keyPair;
//    }

    static String encodeToString(Key key){
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    static RSAPublicKey decodeToPublicKey(String encoded){
        try {
            X509EncodedKeySpec bobPubKeySpec = new X509EncodedKeySpec(
                    Base64.getDecoder().decode(encoded));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA",
                    new BouncyCastleProvider());
            PublicKey publicKey = keyFactory.generatePublic(bobPubKeySpec);
            return (RSAPublicKey)publicKey;
        }catch (Exception e){
            log.error("decode public key error, encoded str: {}",encoded,e);
        }
        return null;
    }

    static RSAPublicKey decodePemToPublicKey(String text){
        try (StringReader stringReader = new StringReader(text);){
            return decodePemToPublicKey(stringReader);
        }catch (Exception e){
            log.error("decode pem public key error, pem content: {}",text,e);
        }
        return null;
    }

    static RSAPublicKey decodePemToPublicKey(InputStream in){
        try (InputStreamReader inputStreamReader = new InputStreamReader(in);){
            return decodePemToPublicKey(inputStreamReader);
        }catch (Exception e){
            log.error("decode pem public key error, pem file: {}",in,e);
        }
        return null;
    }

    static RSAPublicKey decodePemToPublicKey(File file){
        try (FileReader keyReader = new FileReader(file);){
            return decodePemToPublicKey(keyReader);
        }catch (Exception e){
            log.error("decode pem public key error, pem file: {}",file,e);
        }
        return null;
    }
    static RSAPublicKey decodePemToPublicKey(Reader reader){
        try (PemReader pemReader = new PemReader(reader)) {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PemObject pemObject = pemReader.readPemObject();
            byte[] content = pemObject.getContent();
            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(content);
            return  (RSAPublicKey)factory.generatePublic(pubKeySpec);
        }catch (Exception e){
            log.error("decode pem public key error, pem file: {}",reader.toString(),e);
        }
        return null;
    }

    static RSAPrivateKey decodePemToPrivateKey(String text){
        try (StringReader stringReader = new StringReader(text);){
            return decodePemToPrivateKey(stringReader);
        }catch (Exception e){
            log.error("decode pem private key error, pem content: {}",text,e);
        }
        return null;
    }

    static RSAPrivateKey decodePemToPrivateKey(InputStream in){
        try (InputStreamReader inputStreamReader = new InputStreamReader(in);){
            return decodePemToPrivateKey(inputStreamReader);
        }catch (Exception e){
            log.error("decode pem private key error, pem file: {}",in,e);
        }
        return null;
    }

    static RSAPrivateKey decodePemToPrivateKey(File file){
        try (FileReader keyReader = new FileReader(file);){
            return decodePemToPrivateKey(keyReader);
        }catch (Exception e){
            log.error("decode pem private key error, pem file: {}",file,e);
        }
        return null;
    }

    static RSAPrivateKey decodePemToPrivateKey(Reader reader){
        try (PemReader pemReader = new PemReader(reader)) {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PemObject pemObject = pemReader.readPemObject();
            byte[] content = pemObject.getContent();
            PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(content);
            return (RSAPrivateKey) factory.generatePrivate(privKeySpec);
        }catch (Exception e){
            log.error("decode pem private key error, pem file: {}",reader.toString(),e);
        }
        return null;
    }

    static RSAPrivateKey decodeToPrivateKey(String encoded){
        try {
            X509EncodedKeySpec bobPubKeySpec = new X509EncodedKeySpec(
                    Base64.getDecoder().decode(encoded));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA",
                    new BouncyCastleProvider());
            PrivateKey privateKey = keyFactory.generatePrivate(bobPubKeySpec);
            return (RSAPrivateKey)privateKey;
        }catch (Exception e){
            log.error("decode private key error, encoded str: {}",encoded,e);
        }
        return null;
    }

    static byte[] encrypt(Key key, byte[] data) {
        if (key != null) {
            try {
                Cipher cipher = Cipher.getInstance("RSA",
                        new BouncyCastleProvider());
                cipher.init(Cipher.ENCRYPT_MODE, key);
                return cipher.doFinal(data);
            } catch (Exception e) {
               log.error("encrypt data error, key: {}, data: {}",key,data,e);
            }
        }
        return null;
    }
}
