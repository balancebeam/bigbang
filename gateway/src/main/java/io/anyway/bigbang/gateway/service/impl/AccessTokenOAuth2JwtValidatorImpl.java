package io.anyway.bigbang.gateway.service.impl;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.djtgroup.framework.session.UserDetailContext;
import io.anyway.bigbang.framework.utils.RSAUtil;
import io.anyway.bigbang.gateway.service.AccessTokenValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "spring.cloud.gateway.token-validator",name = "mode",havingValue = "jwt")
public class AccessTokenOAuth2JwtValidatorImpl implements AccessTokenValidator, InitializingBean {

    private JWSVerifier verifier;

    @Value("${spring.cloud.gateway.token-validator.public-key-location}")
    private String location;

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
        try(InputStream in= Thread.currentThread().getContextClassLoader().getSystemResourceAsStream(location);){
            PublicKey publicKey= RSAUtil.decodePemToPublicKey(in);
            if(publicKey== null){
                throw new RuntimeException("public key was invalid.");
            }
            verifier= new RSASSAVerifier((RSAPublicKey) publicKey);
            log.info("JWSVerifier: {}",verifier);
        }
    }

//    public static void main(String[] args)throws Exception{
//        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(new ClassPathResource("jwt-cert.jks"), "keystorepass".toCharArray());
//        KeyPair keyPair= keyStoreKeyFactory.getKeyPair("jwt", "keypairpass".toCharArray());
//        String s= RSAUtil.encodeToString(keyPair.getPublic());
//        System.out.println(s);
//    }
}
