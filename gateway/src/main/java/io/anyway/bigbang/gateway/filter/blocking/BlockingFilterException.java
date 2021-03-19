package io.anyway.bigbang.gateway.filter.blocking;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BlockingFilterException extends RuntimeException{

    private HttpStatus httpStatus;

    public BlockingFilterException(HttpStatus httpStatus,String message){
        super(message);
        this.httpStatus= httpStatus;
    }
}
