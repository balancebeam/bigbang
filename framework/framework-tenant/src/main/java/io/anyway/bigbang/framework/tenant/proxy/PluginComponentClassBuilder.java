package io.anyway.bigbang.framework.tenant.proxy;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public interface PluginComponentClassBuilder {

    Class<?> build(AnnotationConfigApplicationContext ctx);
}
