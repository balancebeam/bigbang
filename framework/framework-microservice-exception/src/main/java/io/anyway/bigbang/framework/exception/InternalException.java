package io.anyway.bigbang.framework.exception;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@ToString
@JsonIgnoreProperties
public class InternalException extends RuntimeException {

    private String code;
    private String message;
    private Object body;
    private int httpStatus= HttpStatus.SERVICE_UNAVAILABLE.value();

    public InternalException(){}

    public InternalException(String code, String message){
        this.code= code;
        this.message= message;
    }
}
