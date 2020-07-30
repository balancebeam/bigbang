package io.anyway.bigbang.gateway.filter;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import io.anyway.bigbang.framework.core.security.SecurityContextHolder;
import io.anyway.bigbang.framework.core.security.UserDetail;
import io.anyway.bigbang.framework.core.security.AuthValidationProcessor;
import io.anyway.bigbang.gateway.service.PublicKeyListener;
import io.anyway.bigbang.gateway.service.WhiteListService;
import io.anyway.bigbang.gateway.utils.WebExchangeResponseUtil;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.Security;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class AccessTokenValidationFilter implements GlobalFilter, Ordered, PublicKeyListener, SmartInitializingSingleton {

    @Value("${bigbang.oauth2.service-id:oauth2-srv}")
    private String serviceId;

    @Value("${bigbang.oauth2.rsa-public-key-path:/api/cert/rsa-public-key}")
    private String keyPath;

    @Resource
    private WhiteListService whiteListService;

    @Autowired(required = false)
    private List<AuthValidationProcessor> validationProcessorList= Collections.EMPTY_LIST;

    private volatile RSAPublicKey publicKey;

    @Resource
    private LoadBalancerClient loadBalancerClient;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        SecurityContextHolder.remove();
        if (!whiteListService.isWhitelistApi(exchange)) {
            String accessToken = exchange.getRequest().getHeaders().getFirst("X-Access-Token");
            if(StringUtils.isEmpty(accessToken)){
                accessToken= exchange.getRequest().getQueryParams().getFirst("X-Access-Token");
            }
            if(!StringUtils.isEmpty(accessToken)){
                try{
                    JWSObject jwsObject = JWSObject.parse(accessToken);
                    if(publicKey== null){
                        synchronized (this){
                            if(publicKey== null){
                                change();
                                if(publicKey== null){
                                    throw new Exception("Fetch rsa public key "+keyPath+" error.");
                                }
                            }
                        }
                    }
                    JWSVerifier verifier = new RSASSAVerifier(publicKey);
                    if (jwsObject.verify(verifier)) {
                        JSONObject jsonOBj = jwsObject.getPayload().toJSONObject();
                        if (jsonOBj.containsKey("exp")) {
                            String value= jsonOBj.getAsString("exp");
                            if(value.length()==10){
                                value+= "000";
                            }
                            long extTime = Long.parseLong(value);
                            long curTime = System.currentTimeMillis();
                            if (extTime > curTime) {
                                for(AuthValidationProcessor each: validationProcessorList){
                                    each.valid(jsonOBj);
                                }
                                long userId = jsonOBj.getAsNumber("user_id").longValue();
                                String username= jsonOBj.getAsString("user_name");
                                String userType= jsonOBj.getAsString("user_type");
                                UserDetail userDetail= new UserDetail(userId,username,userType);
                                SecurityContextHolder.setUserDetail(userDetail);
                                log.debug("userDetail: {}",userDetail);
                                return chain.filter(exchange);
                            }
                        }
                    }
                }catch (Exception e){
                    log.error("accessToken was invalid, accessToken: {},error msg: {}",accessToken,e.getMessage());
                }
            }
            return WebExchangeResponseUtil.handleError(exchange,HttpStatus.UNAUTHORIZED,"UNAUTHORIZED.");
        }
        return chain.filter(exchange);

    }

    @Override
    public int getOrder() {
        return -200;
    }

    @Override
    public void afterSingletonsInstantiated() {
        change();
    }

    @Override
    public void change() {
        ServiceInstance serviceInstance = loadBalancerClient.choose(serviceId);
        if(serviceInstance!= null){
            String path= "http://"+serviceInstance.getHost()+":"+serviceInstance.getPort()+keyPath;
            try {
                URL url= new URL(path);
                HttpURLConnection conn= (HttpURLConnection)url.openConnection();
                try(InputStream in= conn.getInputStream();){
                    Security.addProvider(new BouncyCastleProvider());
                    JWKSet jwkSet= JWKSet.load(in) ;
                    RSAKey rsaKey= (RSAKey)jwkSet.getKeys().get(0);
                    BigInteger publicExponent= rsaKey.getPublicExponent().decodeToBigInteger();
                    BigInteger modulus= rsaKey.getModulus().decodeToBigInteger();
                    KeyFactory keyFactory = KeyFactory.getInstance("RSA",new BouncyCastleProvider());
                    RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus,publicExponent);
                    publicKey= (RSAPublicKey) keyFactory.generatePublic(keySpec);
                    log.info("OAuth2 RSAPublicKey: {}",publicKey);
                }
            } catch (Exception e) {
                log.error("Fetch OAuth2 PublicKey error",e);
                throw new RuntimeException(e);
            }
        }
    }
}
