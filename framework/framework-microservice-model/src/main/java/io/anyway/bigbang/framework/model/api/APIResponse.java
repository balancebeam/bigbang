package io.anyway.bigbang.framework.model.api;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class APIResponse<T> {

    private int code;
    private String message;
    private T body;

    public static <E> APIResponse<E> ok(E body){
        APIResponse<E> response= new APIResponse<>();
        response.setCode(0);
        response.setMessage("OK");
        response.setBody(body);
        return response;
    }

    public static APIResponse fail(int code, String message){
        APIResponse response= new APIResponse();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }
}
