package io.anyway.bigbang.oauth2.configuration;

import io.anyway.bigbang.oauth2.domain.XUserDetails;
import io.anyway.bigbang.oauth2.service.impl.XUserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class OAuth2ServiceConfigurer {

    @Value("${bigbang.keystore.cert-path}")
    private String certPath;

    @Value("${bigbang.keystore.password}")
    private String keystorePwd;

    @Value("${bigbang.keystore.keypair.alias}")
    private String keypairAlias;

    @Value("${bigbang.keystore.keypair.password}")
    private String keypairPwd;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AccessTokenConverter createAccessTokenConverter(KeyPair keyPair) {
        JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter(){
            @Override
            public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
                XUserDetails userDetails= (XUserDetails)authentication.getPrincipal();
                final Map<String, Object> additionalInfo = new HashMap<>();
                if(userDetails.getTenantId()!= null) {
                    additionalInfo.put("tenant_id", userDetails.getTenantId());
                }
                additionalInfo.put("user_id", userDetails.getUid());
                additionalInfo.put("user_type", userDetails.getUserType());
                ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
                return super.enhance(accessToken,authentication);
            }
        };
        jwtAccessTokenConverter.setKeyPair(keyPair);
        return jwtAccessTokenConverter;
    }


    @Bean
    public KeyPair keyPair() {
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(new ClassPathResource(certPath), keystorePwd.toCharArray());
        return keyStoreKeyFactory.getKeyPair(keypairAlias, keypairPwd.toCharArray());
    }

    @Bean("xUserDetailsService")
    public UserDetailsService createXUserDetailService(){
        return new XUserDetailsServiceImpl();
    }

}
