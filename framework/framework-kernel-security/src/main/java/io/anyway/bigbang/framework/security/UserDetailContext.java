package io.anyway.bigbang.framework.security;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailContext {

    final public static String USER_HEADER_NAME="x-user-detail";

    private Long uid;
    private String username;
    private String type= "c";

}
