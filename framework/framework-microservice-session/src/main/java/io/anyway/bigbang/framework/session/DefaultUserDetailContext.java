package io.anyway.bigbang.framework.session;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DefaultUserDetailContext implements UserDetailContext{
    private String appId;
    private String userId;
    private String userName;
    private String type= "c";
}
