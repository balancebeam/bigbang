package io.anyway.bigbang.framework.datasource.sharding.annotation;


import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataSourceSharding {

}
