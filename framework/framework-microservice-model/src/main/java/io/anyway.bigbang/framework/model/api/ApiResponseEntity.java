package io.anyway.bigbang.framework.model.api;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class ApiResponseEntity<T> {

    private String code;
    private String message;
    private long duration;
    private T body;

    public static <E> ApiResponseEntity<E> ok(E body){
        ApiResponseEntity<E> response= new ApiResponseEntity<E>();
        response.setCode("0");
        response.setMessage("OK");
        response.setBody(body);
        return response;
    }

    public static ApiResponseEntity fail(String code, String message){
        ApiResponseEntity response= new ApiResponseEntity();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }

    public static ApiResponseEntity fail(String code, String message,Object detail){
        ApiResponseEntity response= new ApiResponseEntity();
        response.setCode(code);
        response.setMessage(message);
        response.setBody(detail);
        return response;
    }
}
