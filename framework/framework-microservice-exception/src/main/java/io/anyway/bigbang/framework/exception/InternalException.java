package io.anyway.bigbang.framework.exception;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@ToString
class InternalException extends RuntimeException {

    private String code;
    private String message;
    private int httpStatus= HttpStatus.SERVICE_UNAVAILABLE.value();

    public InternalException(){}

    public InternalException(String code, String message){
        this.code= code;
        this.message= message;
    }
}
