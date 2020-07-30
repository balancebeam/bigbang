package io.anyway.bigbang.framework.datasource.sharding.aspect;

import io.anyway.bigbang.framework.datasource.sharding.DataSourceShardingContextHolder;
import io.anyway.bigbang.framework.datasource.sharding.annotation.DataSourceSharding;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;


@Slf4j
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnClass(DataSourceSharding.class)
public class DataSourceShardingAspect {

    @Pointcut("@annotation(io.anyway.bigbang.framework.datasource.sharding.annotation.DataSourceSharding) " +
            "|| @within(io.anyway.bigbang.framework.datasource.sharding.annotation.DataSourceSharding)")
    public void shardingPointCut() {}

    @Around("shardingPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        if(DataSourceShardingContextHolder.sharding()){
            return point.proceed();
        }
        try{
            DataSourceShardingContextHolder.markSharding();
            return point.proceed();
        }finally {
            DataSourceShardingContextHolder.remove();
        }
    }
}
