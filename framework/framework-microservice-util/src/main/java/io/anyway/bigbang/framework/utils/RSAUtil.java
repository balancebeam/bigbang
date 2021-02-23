package io.anyway.bigbang.framework.utils;



import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.security.*;
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

    static PublicKey decodeToPublicKey(String encoded){
        try {
            X509EncodedKeySpec bobPubKeySpec = new X509EncodedKeySpec(
                    Base64.getDecoder().decode(encoded));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA",
                    new BouncyCastleProvider());
            PublicKey publicKey = keyFactory.generatePublic(bobPubKeySpec);
            return publicKey;
        }catch (Exception e){
            log.error("decode public key error, encoded str: {}",encoded,e);
        }
        return null;
    }

    static PrivateKey decodeToPrivateKey(String encoded){
        try {
            X509EncodedKeySpec bobPubKeySpec = new X509EncodedKeySpec(
                    Base64.getDecoder().decode(encoded));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA",
                    new BouncyCastleProvider());
            PrivateKey privateKey = keyFactory.generatePrivate(bobPubKeySpec);
            return privateKey;
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
