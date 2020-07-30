package io.anyway.bigbang.framework.core.exception;

import io.anyway.bigbang.framework.core.rest.RestHeader;
import io.anyway.bigbang.framework.core.utils.I18nUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

public interface GlobalExceptionInterceptor {

    Logger log= LoggerFactory.getLogger(GlobalExceptionInterceptor.class);

    static RestHeader handleException(String requestPath, Throwable e){

        if(e instanceof BigbangException){
            RestHeader response=  ((BigbangException)e).getExceptionResponse();
            log.warn("Biz error: {},request path: {}",response,requestPath);
            return response;
        }
        //invalid arguments
        if(e instanceof MethodArgumentNotValidException){
            RestHeader response= new RestHeader();
            response.setErrorCode(BigbangException.INVALID_ARGUMENTS_ERROR_CODE);
            BindingResult result= ((MethodArgumentNotValidException)e).getBindingResult();
            FieldError fieldError= result.getFieldError();
            String defaultMessage= fieldError.getDefaultMessage();
            String msg= StringUtils.isEmpty(defaultMessage)?
                    (!StringUtils.isEmpty(fieldError.getCode()) ?
                            fieldError.getCode() : e.getMessage())
                    : (defaultMessage.startsWith(I18nUtils.I18N_PREFIX)?
                    I18nUtils.getMessage(defaultMessage.substring(I18nUtils.I18N_PREFIX.length()),fieldError.getCode())
                    :defaultMessage);
            response.setMsg(msg);
            log.warn("Invalid arguments error: {},request path: {}",response,requestPath);
            return response;
        }
        //invalid conversion
        if(e instanceof HttpMessageConversionException){
            RestHeader response= new RestHeader();
            response.setErrorCode(BigbangException.INVALID_CONVERSION_ERROR_CODE);
            int idx= e.getMessage().indexOf("nested exception");
            if(idx>0){
                response.setMsg(e.getMessage().substring(0,idx));
            }
            else {
                response.setMsg(e.getMessage());
            }
            log.error("Invalid conversion error: {},request path: {}",response,requestPath);
            return response;
        }
        //default
        RestHeader response= new RestHeader();
        response.setErrorCode(BigbangException.DEFAULT_ERROR_CODE);
        if(e instanceof NullPointerException){
            response.setMsg((e.getStackTrace()!=null && e.getStackTrace().length>0)?("NullPointerException: "+e.getStackTrace()[0].toString()):e.getMessage());
        }
        else {
            response.setMsg(e.getMessage());
        }
        log.error("System error: {},request path: {}",response,requestPath,e);
        return response;
    }
}
