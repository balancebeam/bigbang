package io.anyway.bigbang.framework.exception;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@ToString
public class ApiException extends RuntimeException{

    private HttpStatus httpStatus= HttpStatus.INTERNAL_SERVER_ERROR;
    private String apiResultStatus;
    private Object[] messageResourceArgs;
    private Object detail;

    public ApiException(String apiResultStatus){
        this.apiResultStatus= apiResultStatus;
    }

    public ApiException(HttpStatus httpStatus, String apiResultStatus){
        this.httpStatus= httpStatus;
        this.apiResultStatus= apiResultStatus;
    }

    public ApiException(String apiResultStatus, Throwable e){
        super(e);
        this.apiResultStatus= apiResultStatus;
    }

    public ApiException(HttpStatus httpStatus, String apiResultStatus, Throwable e){
        super(e);
        this.httpStatus= httpStatus;
        this.apiResultStatus= apiResultStatus;
    }

    public ApiException(String apiResultStatus, Object[] messageResourceArgs){
        this.apiResultStatus= apiResultStatus;
        this.messageResourceArgs= messageResourceArgs;
    }

    public ApiException(HttpStatus httpStatus, String apiResultStatus, Object[] messageResourceArgs){
        this.httpStatus= httpStatus;
        this.apiResultStatus= apiResultStatus;
        this.messageResourceArgs= messageResourceArgs;
    }

    public ApiException(String apiResultStatus, Object[] messageResourceArgs, Throwable e){
        super(e);
        this.apiResultStatus= apiResultStatus;
        this.messageResourceArgs= messageResourceArgs;
    }

    public ApiException(HttpStatus httpStatus, String apiResultStatus, Object[] messageResourceArgs, Throwable e){
        super(e);
        this.httpStatus= httpStatus;
        this.apiResultStatus= apiResultStatus;
        this.messageResourceArgs= messageResourceArgs;
    }


}
