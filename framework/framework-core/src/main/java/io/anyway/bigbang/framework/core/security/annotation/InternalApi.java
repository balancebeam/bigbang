package io.anyway.bigbang.framework.core.security.annotation;


import java.lang.annotation.*;

@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InternalApi {
    String HEADER_GATEWAY_KEY= "X-Origin-Gateway";
}
