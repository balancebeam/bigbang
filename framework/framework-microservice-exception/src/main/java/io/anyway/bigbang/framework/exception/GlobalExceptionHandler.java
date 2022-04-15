package io.anyway.bigbang.framework.exception;

import io.anyway.bigbang.framework.model.api.ApiResponseEntity;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
@ConditionalOnClass(HttpServletRequest.class)
public class GlobalExceptionHandler implements MeterBinder {

    private static String CONTENT_TYPE= "application/json;charset=UTF-8";

    @Resource
    private MessageSource messageSource;

    private MeterRegistry meterRegistry;

    private volatile Map<String,Counter> exceptionCounterMap= new HashMap<>();

    @ResponseBody
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ApiResponseEntity processRequestParameterException(
            HttpServletRequest request,
            HttpServletResponse response,
            MissingServletRequestParameterException e){
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(CONTENT_TYPE);
        log.warn("Illegal Arguments Exception, url: {}, reason: {}",request.getRequestURI(), getAvailableCause(e));
        return ApiResponseEntity.fail(
                HttpStatus.FORBIDDEN.value()+"",
                messageSource.getMessage("BAD_REQUEST_PARAMETER",
                        null,
                        HttpStatus.FORBIDDEN.value()+" "+e.getClass().getName()+": "+ getAvailableCause(e),
                        LocaleContextHolder.getLocale()));
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ApiResponseEntity processDefaultException(
            HttpServletRequest request,
            HttpServletResponse response,
            Exception e) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setContentType(CONTENT_TYPE);
        log.error("Unknown Type Exception, url: {}",request.getRequestURI(),e);
        doExceptionMetrics("unknown",e);
        return ApiResponseEntity.fail(
                HttpStatus.INTERNAL_SERVER_ERROR.value()+"",
                messageSource.getMessage("INTERNAL_SERVER_ERROR",
                        null,
                        HttpStatus.INTERNAL_SERVER_ERROR.value()+" "+e.getClass().getName()+": " +getAvailableCause(e),
                        LocaleContextHolder.getLocale()));
    }


    @ResponseBody
    @ExceptionHandler(ApiException.class)
    public ApiResponseEntity processApiException(
            HttpServletRequest request,
            HttpServletResponse response,
            ApiException e) {

        response.setStatus(e.getHttpStatus().value());
        response.setContentType(CONTENT_TYPE);

        String message = messageSource.getMessage(
                e.getApiResultStatus(),
                e.getMessageResourceArgs(),
                e.getApiResultStatus()+" "+e.getClass().getName()+": "+ getAvailableCause(e),
                LocaleContextHolder.getLocale());
        log.warn("ApiException, url: {}, reason: {}",request.getRequestURI(),message);
        doExceptionMetrics("api",e);
        return ApiResponseEntity.fail(e.getApiResultStatus(),message,e.getDetail());
    }

    @ResponseBody
    @ExceptionHandler(InternalException.class)
    public ApiResponseEntity processDeliveryException(
            HttpServletRequest request,
            HttpServletResponse response,
            InternalException e) {

        response.setStatus(e.getHttpStatus());
        response.setContentType(CONTENT_TYPE);

        return ApiResponseEntity.fail(e.getCode(),e.getMessage(),e.getBody());
    }

    private String getAvailableCause(Throwable e){
        String message = null;
        while(e!= null && (message=e.getMessage())==null){
            e = e.getCause();
        }
        return message== null ? "" : message;
    }

    private void doExceptionMetrics(String type,Exception e){
        String counterName= "exception."+type+"."+e.getClass().getSimpleName();
        Counter counter= exceptionCounterMap.get(counterName);
        if(counter==null){
            synchronized (exceptionCounterMap) {
                if((counter=exceptionCounterMap.get(counterName))== null) {
                    counter = Counter.builder(counterName).register(meterRegistry);
                    exceptionCounterMap.put(counterName,counter);
                }
            }
        }
        counter.increment();
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        this.meterRegistry = registry;
    }
}
