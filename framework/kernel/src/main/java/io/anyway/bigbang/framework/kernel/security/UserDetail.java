package io.anyway.bigbang.framework.kernel.security;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserDetail {

    final public static String USER_HEADER_NAME="x-user-detail";

    private Long uid;
    private String username;
    private String type= "c";

}
