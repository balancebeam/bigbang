package io.anyway.bigbang.framework.bootstrap;

public interface HeaderContext {

    String getName();

    default void removeThreadLocal(){};

}
