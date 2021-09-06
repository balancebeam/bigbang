package io.anyway.bigbang.gateway.service.impl;

import io.anyway.bigbang.framework.session.UserDetailContext;
import io.anyway.bigbang.framework.utils.JsonUtil;
import io.anyway.bigbang.gateway.service.AccessTokenValidator;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import javax.annotation.Resource;
import java.util.Optional;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "spring.cloud.gateway.token-validator",name = "mode",havingValue = "api")
public class AccessTokenApiServiceValidatorImpl implements AccessTokenValidator {

    private String WEB_ACCESS_KEY_CACHE_KEY = "industry:web:user:cache:%s";

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public Optional<UserDetailContext> check(ServerWebExchange exchange) {
        String accessKey= exchange.getRequest().getHeaders().getFirst("accessKey");
        if(StringUtils.isEmpty(accessKey)) {
            return Optional.empty();
        }
        String redisKey = String.format(WEB_ACCESS_KEY_CACHE_KEY, accessKey);
        String cacheStr =(String)redisTemplate.opsForValue().get(redisKey);
        if(StringUtils.isEmpty(cacheStr)){
            return Optional.empty();
        }
        return Optional.of(JsonUtil.fromString2Object(cacheStr,WebUserBO.class));
    }

    @Data
    public static class WebUserBO implements UserDetailContext {
        private String userId;

        private String nickName;

        private String wxOpenId;

        /**
         * 用户头像
         */
        private String avatarUrl;

        private String phoneNum;

        private Integer userType;

        /**
         * 用户类型描述
         */
        private String userTypeDesc;

        private Integer staffType;

        private Integer department;

        private String accessKey;

        private String sessionKey;

        @Override
        public String getUserName() {
            return nickName;
        }
    }
}
