package io.anyway.bigbang.framework.logging.marker;

import io.anyway.bigbang.framework.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.util.StopWatch;

import java.lang.reflect.Method;
import java.util.Objects;

@Slf4j
@Aspect
public class LoggingMarkerAspect {

    @Pointcut("@annotation(io.anyway.bigbang.framework.logging.marker.LoggingMarker) " +
            "|| @within(io.anyway.bigbang.framework.logging.marker.LoggingMarker)")
    public void pointcut(){}

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable{
        Signature signature= point.getSignature();
        Object target= point.getTarget();
        String className= "";
        String methodName= "";
        Object[] args= point.getArgs();
        String[] markers= {};
        String description= "";
        if(target!= null){
            className= target.getClass().getName();
            LoggingMarker loggingMarker=target.getClass().getAnnotation(LoggingMarker.class);
            if(Objects.nonNull(loggingMarker)){
                markers= loggingMarker.markers();
                description= loggingMarker.value();
            }
        }
        if(signature instanceof MethodSignature){
            MethodSignature methodSignature= (MethodSignature)signature;
            Method method= methodSignature.getMethod();
            LoggingMarker loggingMarker=method.getAnnotation(LoggingMarker.class);
            if(Objects.nonNull(loggingMarker)){
                markers= loggingMarker.markers();
                description= loggingMarker.value();
            }
        }
        LoggingMarkerContext.markers(markers);
        log.info(new LoggingMarkerWrapper(description,"IN"),"{}#{},args: {}",
                className,methodName,
                JsonUtil.fromObject2String(args));
        StopWatch stopWatch= new StopWatch();
        stopWatch.start();
        try{
            return point.proceed();
        }finally {
            stopWatch.stop();
            long elapseMs= stopWatch.getTotalTimeMillis();
            log.info(new LoggingMarkerWrapper(description,"OUT"),"{}${},consume {} ms.",className,methodName,elapseMs);
            LoggingMarkerContext.remove();
        }
    }
}
