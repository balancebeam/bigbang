package io.anyway.bigbang.framework.model.api;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonIgnoreProperties
@ApiModel(value = "ApiResponseEntity",description = "Api response body attribute description")
public class ApiResponseEntity<T> {

    @ApiModelProperty(value = "biz code",example = "0")
    private String code;
    @ApiModelProperty(value = "biz tip message",example = "Success")
    private String message;
    @ApiModelProperty(value = "biz duration",example = "201")
    private Long duration;
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

