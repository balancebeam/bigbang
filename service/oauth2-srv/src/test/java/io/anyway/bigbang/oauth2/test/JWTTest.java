package io.anyway.bigbang.oauth2.test;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import net.minidev.json.JSONObject;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;

public class JWTTest {

    private KeyPair getKeyPair(){
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(new ClassPathResource("certificate/jwt-cert.jks"), "keystorepass".toCharArray());
        KeyPair keyPair= keyStoreKeyFactory.getKeyPair("jwt", "keypairpass".toCharArray());
        return keyPair;
    }

    private String getPubKeyJson() {
        RSAPublicKey publicKey = (RSAPublicKey) getKeyPair().getPublic();
        RSAKey key = new RSAKey.Builder(publicKey).build();
        return new JWKSet(key).toJSONObject().toJSONString();
    }

    private RSAPublicKey getRSAPublicKey(){
        try {
            String txt = getPubKeyJson();
            JWKSet jwKSet = JWKSet.parse(txt);
            RSAKey rsaKey = (RSAKey) jwKSet.getKeys().get(0);
            BigInteger publicExponent= rsaKey.getPublicExponent().decodeToBigInteger();
            BigInteger modulus= rsaKey.getModulus().decodeToBigInteger();
            KeyFactory keyFactory = KeyFactory.getInstance("RSA", new BouncyCastleProvider());
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus, publicExponent);
            RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
            return publicKey;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private void valid(){
        try {
            RSAPublicKey publicKey = getRSAPublicKey();
            JWSObject jwsObject = JWSObject.parse("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1ODEzNjc4NjEsInVzZXJfbmFtZSI6Inlhbmd6eiIsImp0aSI6ImYzYjJlYWU1LWQyMjgtNDkwYi1hYjAzLWNhYjJiZDEzNzAzYiIsImNsaWVudF9pZCI6ImFwaS1nYXRld2F5Iiwic2NvcGUiOlsicmVhZF91c2VyaW5mbyJdfQ.GUnygcxWtW0NEWcY84gIzvyHcZgK6NaLlTBUqo4kAnuEkFl3HKa95XOTMJiEvUy4X86W8uLDNuReyhj2rwNwd1caRjsE8Tzdm-eaXL_nXgsBbRx9aGAQxaBsN_L9hpWHJxW2bVEN1n0sLEA9NnMFdZGcqY0d5Hyh31SCasnYSmtYJYrG04ZkvxA_71Ud5TbLBDK6H5pV0hK_S9gisLvGNc46HHWYUzWeegZElw8OuxDSW5gsarrVxuoh3afsL6sa9SpvJIEWuvj8qYe203p-_nLY4A_nEeMJ2cmGqQDQxFXm9w28G-ZkmaBtyxvJIe78h-u4YKIQSTol4xDthAs2mQ");
            JWSVerifier verifier = new RSASSAVerifier(publicKey);
            if (jwsObject.verify(verifier)) {
                JSONObject jsonOBj = jwsObject.getPayload().toJSONObject();
                if (jsonOBj.containsKey("exp")) {
                    long extTime = jsonOBj.getAsNumber("exp").longValue();
                    long curTime = System.currentTimeMillis();
                    if (curTime > extTime) {
                        System.out.println("");
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        JWTTest test= new JWTTest();
        test.valid();
    }
}
