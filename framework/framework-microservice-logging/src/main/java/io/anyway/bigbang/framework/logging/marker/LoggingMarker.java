package io.anyway.bigbang.framework.logging.marker;

import java.lang.annotation.*;

@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LoggingMarker {

    String value();

    String[] markers() default {};
}
