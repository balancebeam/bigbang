package io.anyway.bigbang.framework.mqclient.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Listener {

    String value();

    String[] tags() default {};
}
