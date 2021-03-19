package io.anyway.bigbang.gateway.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

@Getter
@Setter
@ToString
public class ApiResource {
    private String serviceId;
    private String code;
    private String path;
    private Pattern matcher;
    private Route route;

    public ApiResource(){

    }
    public ApiResource(String serviceId, String code){
        this(serviceId,code,"");
    }

    public ApiResource(String serviceId, String code, String path){
        this(serviceId,code,path,null);
    }

    public ApiResource(String serviceId, String code, String path, String m){
        this.serviceId= serviceId;
        this.code= code;
        this.path= path;
        if(StringUtils.hasLength(m)) {
            this.matcher = Pattern.compile(m);
        }
    }

}
