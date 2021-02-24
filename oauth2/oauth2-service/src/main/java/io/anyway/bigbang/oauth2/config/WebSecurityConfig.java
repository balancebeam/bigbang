package io.anyway.bigbang.oauth2.config;

import io.anyway.bigbang.oauth2.domain.XUserDetails;
import io.anyway.bigbang.oauth2.service.XUserDetailsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.security.KeyPair;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


@Slf4j
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public XUserDetailsServiceImpl createXUserDetailService() {
        return new XUserDetailsServiceImpl();
    }



    @Bean
    public TokenEnhancer createTokenEnhancer() {
        return (accessToken, authentication) -> {
            if (authentication.getPrincipal() instanceof XUserDetails) {
                enhanceAccessToken(accessToken, authentication);
            }
            return accessToken;
        };
    }

    private static void enhanceAccessToken(OAuth2AccessToken accessToken,OAuth2Authentication authentication){
        XUserDetails userDetail = (XUserDetails) authentication.getPrincipal();
        final Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("user_id", userDetail.getUsername());
        additionalInfo.put("appId", userDetail.getAppId());
        if (userDetail.getLoginName() != null) {
            additionalInfo.put("user_name", userDetail.getLoginName());
        }
        if (userDetail.getUserType() != null) {
            additionalInfo.put("user_type", userDetail.getUserType());
        }
        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
    }

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        localeResolver.setDefaultLocale(Locale.CHINA);
        return localeResolver;
    }

    @Bean
    public RestTemplate restTemplate() throws Exception {
        CloseableHttpClient httpClient = null;
        try {
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustAllStrategy());

            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    builder.build(), NoopHostnameVerifier.INSTANCE);
            httpClient = HttpClients.custom().setSSLSocketFactory(
                    sslsf).build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(factory);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors();
        http.authorizeRequests()
                .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
                .antMatchers("/api/**").permitAll()
                .anyRequest().authenticated()
                .and().csrf().disable();
        ;
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public SessionRegistry getSessionRegistry() {
        SessionRegistry sessionRegistry = new SessionRegistryImpl();
        return sessionRegistry;
    }

    @Configuration
    @ConditionalOnProperty(prefix= "spring.security.oauth2.jwt", name = "enabled",havingValue="true" ,matchIfMissing = false)
    public static class JwtConfig{
        @Value("${spring.security.oauth2.jwt.keystore-path:jwt-cert.jks}")
        private String certPath;

        @Value("${spring.security.oauth2.jwt.keystore-password:keystorepass}")
        private String keystorePwd;

        @Value("${spring.security.oauth2.jwt.keypair-alias:jwt}")
        private String keypairAlias;

        @Value("${spring.security.oauth2.jwt.keypair-password:keypairpass}")
        private String keypairPwd;

        @Bean
        public KeyPair keyPair() {
            KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(new ClassPathResource(certPath), keystorePwd.toCharArray());
            return keyStoreKeyFactory.getKeyPair(keypairAlias, keypairPwd.toCharArray());
        }

        @Bean
        public AccessTokenConverter createJwtAccessTokenConverter(KeyPair keyPair) {
            JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter(){
                @Override
                public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
                    enhanceAccessToken(accessToken, authentication);
                    return super.enhance(accessToken,authentication);
                }
            };
            jwtAccessTokenConverter.setKeyPair(keyPair);
            return jwtAccessTokenConverter;
        }
    }
}
