package io.anyway.bigbang.framework.kernel.exception;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@ToString
public class ApiException extends RuntimeException{

    private int httpStatus= HttpStatus.INTERNAL_SERVER_ERROR.value();
    private int apiResultStatus;
    private Object[] messageResourceArgs;

    public ApiException(int apiResultStatus){
        this.apiResultStatus= apiResultStatus;
    }

    public ApiException(int httpStatus, int apiResultStatus){
        this.httpStatus= httpStatus;
        this.apiResultStatus= apiResultStatus;
    }

    public ApiException(int apiResultStatus, Throwable e){
        super(e);
        this.apiResultStatus= apiResultStatus;
    }

    public ApiException(int httpStatus, int apiResultStatus, Throwable e){
        super(e);
        this.httpStatus= httpStatus;
        this.apiResultStatus= apiResultStatus;
    }

    public ApiException(int apiResultStatus, Object[] messageResourceArgs){
        this.apiResultStatus= apiResultStatus;
        this.messageResourceArgs= messageResourceArgs;
    }

    public ApiException(int httpStatus, int apiResultStatus, Object[] messageResourceArgs){
        this.httpStatus= httpStatus;
        this.apiResultStatus= apiResultStatus;
        this.messageResourceArgs= messageResourceArgs;
    }

    public ApiException(int apiResultStatus, Object[] messageResourceArgs, Throwable e){
        super(e);
        this.apiResultStatus= apiResultStatus;
        this.messageResourceArgs= messageResourceArgs;
    }

    public ApiException(int httpStatus, int apiResultStatus, Object[] messageResourceArgs, Throwable e){
        super(e);
        this.httpStatus= httpStatus;
        this.apiResultStatus= apiResultStatus;
        this.messageResourceArgs= messageResourceArgs;
    }


}
