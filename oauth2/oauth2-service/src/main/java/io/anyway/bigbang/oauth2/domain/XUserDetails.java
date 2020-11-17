package io.anyway.bigbang.oauth2.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;


@Getter
@Setter
@ToString
public class XUserDetails implements UserDetails {

    /**
     * 固定字段, 不可修改. 实际上对应了 user_id 参数.
     */
    private String username;
    /**
     * 固定字段, 不可修改. 加密后的密码
     */
    private String password;

    /**
     * 租户 id, 用于标示不同的系统, 即 app_id 字段
     */
    private String tenantId;
    /**
     * 对应了 username 字段. 因为 username 为固定变量, 因此换个名字.
     */
    private String loginName;
    /**
     * 用户类型
     */
    private String userType;

    private boolean enabled = true;
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
