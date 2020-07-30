package io.anyway.bigbang.framework.core.exception;

import io.anyway.bigbang.framework.core.rest.RestHeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@ControllerAdvice
public class ControllerExceptionAdvice {

    @ResponseBody
    @ExceptionHandler(Throwable.class)
    public RestHeader handleException(Throwable e){
        String requestPath= "";
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        if(requestAttributes!= null){
            HttpServletRequest request = requestAttributes.getRequest();
            if(request!= null){
                requestPath= request.getRequestURI();
            }
        }
        return GlobalExceptionInterceptor.handleException(requestPath,e);
    }

}
