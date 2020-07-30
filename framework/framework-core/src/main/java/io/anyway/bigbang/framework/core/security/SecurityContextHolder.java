package io.anyway.bigbang.framework.core.security;

public class SecurityContextHolder {

    final private static ThreadLocal<UserDetail> context = new ThreadLocal<>();

    final public static UserDetail getUserDetail() {
        return context.get();
    }

    final public static void setUserDetail(UserDetail userDetail) {
        context.set(userDetail);
    }

    final public static void remove() {
        context.remove();
    }

}
