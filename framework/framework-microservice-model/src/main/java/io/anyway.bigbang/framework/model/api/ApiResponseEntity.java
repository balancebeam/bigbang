package io.anyway.bigbang.framework.model.api;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@ApiModel(value = "ApiResponseEntity",description = "Api Response Body attribute description")
public class ApiResponseEntity<T> {

    @ApiModelProperty(value = "biz code",example = "E0001")
    private String code;
    @ApiModelProperty(value = "biz tip message",example = "Success")
    private String message;
    @ApiModelProperty(value = "biz duration",example = "200")
    private long duration;
    @ApiModelProperty(value = "biz response body",example = "{\"name\":\"Jerry\",\"age\": 21}")
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
