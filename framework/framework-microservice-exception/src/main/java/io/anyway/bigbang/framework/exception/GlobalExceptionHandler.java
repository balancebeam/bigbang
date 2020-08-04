package io.anyway.bigbang.framework.exception;

import io.anyway.bigbang.framework.model.api.APIResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@ControllerAdvice
@ConditionalOnClass(ControllerAdvice.class)
public class GlobalExceptionHandler {

    private static String CONTENT_TYPE= "application/json;charset=UTF-8";

    @Resource
    private MessageSource messageSource;

    @ResponseBody
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public APIResponse processRequestParameterException(
            HttpServletRequest request,
            HttpServletResponse response,
            MissingServletRequestParameterException e){
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(CONTENT_TYPE);

        return APIResponse.fail(
                HttpStatus.FORBIDDEN.value(),
                messageSource.getMessage("BAD_REQUEST_PARAMETER",
                        null,
                        e.getMessage(),
                        LocaleContextHolder.getLocale()));
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public APIResponse processDefaultException(
            HttpServletRequest request,
            HttpServletResponse response,
            Exception e) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setContentType(CONTENT_TYPE);

        return APIResponse.fail(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                messageSource.getMessage("INTERNAL_SERVER_ERROR",
                    null,
                    e.getMessage(),
                    LocaleContextHolder.getLocale()));
    }


    @ResponseBody
    @ExceptionHandler(ApiException.class)
    public APIResponse processApiException(
            HttpServletRequest request,
            HttpServletResponse response,
            ApiException e) {

        response.setStatus(e.getHttpStatus().value());
        response.setContentType(CONTENT_TYPE);

        String message = messageSource.getMessage(
                String.valueOf(e.getApiResultStatus()),
                e.getMessageResourceArgs(),
                e.getApiResultStatus()+ (!StringUtils.isEmpty(e.getMessage())?"_"+e.getMessage() :""),
                LocaleContextHolder.getLocale());
        return APIResponse.fail(e.getApiResultStatus(),message);
    }

    @ResponseBody
    @ExceptionHandler(InternalException.class)
    public APIResponse processMicroServiceException(
            HttpServletRequest request,
            HttpServletResponse response,
            InternalException e) {

        response.setStatus(e.getHttpStatus());
        response.setContentType(CONTENT_TYPE);

        return APIResponse.fail(e.getCode(),e.getMessage());
    }



}
