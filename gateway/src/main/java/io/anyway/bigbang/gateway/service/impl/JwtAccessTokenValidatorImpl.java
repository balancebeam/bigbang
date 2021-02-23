package io.anyway.bigbang.gateway.service.impl;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import io.anyway.bigbang.framework.security.UserDetailContext;
import io.anyway.bigbang.framework.utils.RSAUtil;
import io.anyway.bigbang.gateway.service.AccessTokenValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "spring.cloud.gateway.token-validator",name = "mode",havingValue = "jwt")
public class JwtAccessTokenValidatorImpl implements AccessTokenValidator, InitializingBean {

    private JWSVerifier verifier;

    @Value("${spring.cloud.gateway.token-validator.jwt.jws-algorithm:MAC}")
    private String algorithm;

    @Value("${spring.cloud.gateway.token-validator.jwt.public-key}")
    private String encoded;

    @Override
    public Optional<UserDetailContext> check(String accessToken) {
        try {
            JWSObject jwsObject = JWSObject.parse(accessToken);
            if (jwsObject.verify(verifier)) {
                Map<String, Object> jsonOBj = jwsObject.getPayload().toJSONObject();
                if (jsonOBj.containsKey("exp")) {
                    String value = jsonOBj.get("exp").toString();
                    if (value.length() == 10) {
                        value += "000";
                    }
                    long extTime = Long.parseLong(value);
                    long curTime = System.currentTimeMillis();
                    if (extTime > curTime) {
                        String appId = (String)jsonOBj.get("appId");
                        String userId = (String)jsonOBj.get("user_id");
                        String username = (String)jsonOBj.get("user_name");
                        String userType = (String)jsonOBj.get("user_type");
                        UserDetailContext userDetail = new UserDetailContext(appId,userId, username, userType);
                        log.debug("jwt UserDetail: {}", userDetail);
                        return Optional.of(userDetail);
                    }
                }
            }
        } catch (Exception e){
            log.error("accessToken was invalid, accessToken: {}",accessToken,e);
        }
        return Optional.empty();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        verifier= "RSA".equalsIgnoreCase(algorithm)? createRSAVerifier(encoded): createMACVerifier(encoded);
        log.info("verifier: {}",verifier);
    }

    private JWSVerifier createMACVerifier(String encoded)throws Exception{
        return new MACVerifier(encoded);
    }

    private JWSVerifier createRSAVerifier(String encoded)throws Exception{
        PublicKey publicKey= RSAUtil.decodeToPublicKey(encoded);
        if(publicKey== null){
            throw new RuntimeException("encoded was invalid.");
        }
        return new RSASSAVerifier((RSAPublicKey) publicKey);
    }

//    private JWSVerifier createRSAJWSVerifier(String secret)throws Exception{
//        Security.addProvider(new BouncyCastleProvider());
//        JWKSet jwkSet=  JWKSet.parse(secret);
//        RSAKey rsaKey= (RSAKey)jwkSet.getKeys().get(0);
//        BigInteger publicExponent= rsaKey.getPublicExponent().decodeToBigInteger();
//        BigInteger modulus= rsaKey.getModulus().decodeToBigInteger();
//        KeyFactory keyFactory = KeyFactory.getInstance("RSA",new BouncyCastleProvider());
//        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus,publicExponent);
//        RSAPublicKey publicKey= (RSAPublicKey) keyFactory.generatePublic(keySpec);
//        publicKey.getEncoded();
//        return new RSASSAVerifier(publicKey);
//    }
}
