package io.anyway.bigbang.framework.core.security;

import com.alibaba.fastjson.JSONObject;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserDetail {
    final public static String USER_HEADER_NAME="X-Auth-UserDetail";

    private Long uid;
    private String username;
    private String type;

    public String toJson(){
       return JSONObject.toJSONString(this);
    }
}
