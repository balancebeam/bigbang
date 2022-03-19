package io.anyway.bigbang.framework.utils;

import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;

public abstract class SpringUtil {

    private SpringUtil(){}

    public static <T> T getProxyTarget(Object proxy) {
        if (!AopUtils.isAopProxy(proxy)) {
            return (T) proxy;
        }
        TargetSource targetSource = ((Advised) proxy).getTargetSource();

        try {
            return (T) targetSource.getTarget();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
