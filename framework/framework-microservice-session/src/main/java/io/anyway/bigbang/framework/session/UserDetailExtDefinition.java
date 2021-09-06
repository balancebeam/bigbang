package io.anyway.bigbang.framework.session;

@FunctionalInterface
public interface UserDetailExtDefinition {

    Class<? extends UserDetailContext> def();
}
