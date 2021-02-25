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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
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
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import javax.annotation.Resource;
import java.security.KeyPair;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


@Slf4j
@Configuration
@EnableWebSecurity
@AutoConfigureBefore({WebSecurityConfig.JwtTokenConfig.class, WebSecurityConfig.XDefaultTokenConfig.class})
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Resource
    private RedisConnectionFactory redisConnectionFactory;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public XUserDetailsServiceImpl createXUserDetailService() {
        return new XUserDetailsServiceImpl();
    }

    @Bean
    public TokenStore createDefaultTokenStore() {
        return new RedisTokenStore(redisConnectionFactory);
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
    @ConditionalOnProperty(prefix= "spring.security.oauth2.jwt", name = "enabled",havingValue="true")
    public static class JwtTokenConfig{
        @Value("${spring.security.oauth2.jwt.keystore-path:jwt-cert.jks}")
        private String certPath;

        @Value("${spring.security.oauth2.jwt.keystore-password:keystorepass}")
        private String keystorePwd;

        @Value("${spring.security.oauth2.jwt.keypair-alias:jwt}")
        private String keypairAlias;

        @Value("${spring.security.oauth2.jwt.keypair-password:keypairpass}")
        private String keypairPwd;

        @Resource
        private XUserDetailsServiceImpl userDetailsService;

        @Resource
        private AuthenticationManager authenticationManager;

        @Resource
        private TokenStore tokenStore;

        @Bean
        public KeyPair keyPair() {
            KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(new ClassPathResource(certPath), keystorePwd.toCharArray());
            return keyStoreKeyFactory.getKeyPair(keypairAlias, keypairPwd.toCharArray());
        }

        @Bean("jwtAccessTokenConverter")
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

        @Bean
        public XAuthorizationServerConfigurer.TokenConfigure createJwtTokenConfigure(
                @Qualifier("jwtAccessTokenConverter") AccessTokenConverter accessTokenConverter){
            return endpoints -> endpoints
                    .tokenStore(tokenStore)
                    .authenticationManager(authenticationManager)
                    .userDetailsService(userDetailsService)
                    .accessTokenConverter(accessTokenConverter);
        }
    }

    @Configuration
    public static class XDefaultTokenConfig {

        @Resource
        private TokenStore tokenStore;

        @Resource
        private XUserDetailsServiceImpl userDetailsService;

        @Resource
        private AuthenticationManager authenticationManager;

        @Bean
        @ConditionalOnMissingBean
        public XAuthorizationServerConfigurer.TokenConfigure createDefaultTokenConfigure(){
            TokenEnhancer tokenEnhancer= createDefaultTokenEnhancer();
            return endpoints -> endpoints
                    .tokenServices(defaultTokenServices(tokenEnhancer))
                    .tokenStore(tokenStore)
                    .authenticationManager(authenticationManager)
                    .userDetailsService(userDetailsService)
                    .tokenEnhancer(tokenEnhancer);
        }

        private TokenEnhancer createDefaultTokenEnhancer() {
            return (accessToken, authentication) -> {
                if (authentication.getPrincipal() instanceof XUserDetails) {
                    enhanceAccessToken(accessToken, authentication);
                }
                return accessToken;
            };
        }

        private DefaultTokenServices defaultTokenServices(TokenEnhancer tokenEnhancer) {
            DefaultTokenServices tokenServices = new DefaultTokenServices();
            tokenServices.setTokenStore(tokenStore);
            tokenServices.setSupportRefreshToken(true);
            tokenServices.setAccessTokenValiditySeconds(60 * 60 * 24 * 7);
            tokenServices.setRefreshTokenValiditySeconds(60 * 60 * 24 * 7);
            tokenServices.setTokenEnhancer(tokenEnhancer);
            return tokenServices;
        }
    }
}
