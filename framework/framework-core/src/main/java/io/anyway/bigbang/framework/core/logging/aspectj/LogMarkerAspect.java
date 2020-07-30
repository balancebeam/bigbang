//package io.anyway.bigbang.framework.core.logging.aspectj;
//
//
//import io.anyway.bigbang.framework.core.logging.annotation.LogMarker;
//import lombok.extern.slf4j.Slf4j;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.Signature;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Pointcut;
//import org.aspectj.lang.reflect.MethodSignature;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StopWatch;
//
//import java.lang.reflect.Method;
//import java.util.Objects;
//
//
//@Aspect
//@Component
//@Slf4j
//public class LogMarkerAspect {
//    @Pointcut("@annotation(io.anyway.bigbang.framework.core.logging.annotation.LogMarker) || @within(io.anyway.bigbang.framework.core.logging.annotation.LogMarker)")
//    public void logMarkerPointCut() {
//    }
//
//    @Around("logMarkerPointCut()")
//    public Object around(ProceedingJoinPoint point) throws Throwable {
//        Signature signature = point.getSignature();
//        String description = "";
//        String className = "";
//        String methodName = "";
//        Object[] args = point.getArgs();
//        String marker = "";
//
//        Object target = point.getTarget();
//        if (target != null) {
//            className = target.getClass().getName();
//            LogMarker logMarker = target.getClass().getAnnotation(LogMarker.class);
//            if (Objects.nonNull(logMarker)) {
//                marker = logMarker.marker();
//                description = logMarker.value();
//            }
//        }
//        if (signature instanceof MethodSignature) {
//            MethodSignature methodSignature = (MethodSignature) signature;
//            methodName = methodSignature.getName();
//
//            // SysLog
//            Method method = methodSignature.getMethod();
//            LogMarker logMarker = method.getAnnotation(LogMarker.class);
//            if (Objects.nonNull(logMarker)) {
//                marker = logMarker.marker();
//                description = logMarker.value();
//            }
//        }
//        Markers.addMdcMarker(marker);
//
//        log.info(Markers.build(description, "IN"), "{}#{},args:{}", className, methodName, JsonUtil.toJson(args));
//
//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start();
//
//        try {
//            return point.proceed();
//        } finally {
//            stopWatch.stop();
//            long elapsedMs = stopWatch.getTotalTimeMillis();
//            log.info(Markers.build(description, "OUT"), "{}#{},consume {} ms.", className, methodName, elapsedMs);
//            Markers.removeMdcMarker(marker);
//        }
//    }
//}
