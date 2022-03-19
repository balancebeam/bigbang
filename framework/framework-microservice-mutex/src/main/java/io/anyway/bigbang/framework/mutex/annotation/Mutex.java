package io.anyway.bigbang.framework.mutex.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD})
@Retention(RUNTIME)
@Documented
public @interface Mutex {
    /**
     * 互斥体的名称
     * @return
     */
    String value();

    /**
     * 心跳时间，时间单位秒
     * @return
     */
    int heartbeat() default 5;
}
