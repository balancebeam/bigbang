package io.anyway.bigbang.oauth2.entity;

import io.anyway.bigbang.framework.model.entity.AbstractEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserDetailEntity extends AbstractEntity {
    private String appId;
    private String userId;
    private String userName;
    private String password;
    private String status;
}
