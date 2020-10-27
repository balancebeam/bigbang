package io.anyway.bigbang.oauth2.service;


import io.anyway.bigbang.oauth2.domain.XUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class MobileTokenGranterImpl extends AbstractTokenGranter {

    private static final String GRANT_TYPE = "mobile";

    public MobileTokenGranterImpl(
            @Qualifier("defaultAuthorizationServerTokenServices") AuthorizationServerTokenServices tokenServices,
            ClientDetailsService clientDetailsService,
            OAuth2RequestFactory requestFactory) {
        super(tokenServices, clientDetailsService, requestFactory, GRANT_TYPE);
    }

    @Override
    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {


        XUserDetails details = new XUserDetails();
        //makeup userdetail
        UsernamePasswordAuthenticationToken thirdUserAuth = new UsernamePasswordAuthenticationToken(details, "");
        OAuth2Request storedOAuth2Request = getRequestFactory().createOAuth2Request(client, tokenRequest);
        return new OAuth2Authentication(storedOAuth2Request, thirdUserAuth);
    }
}
