package io.anyway.bigbang.framework.header;

public interface HeaderContext {

    String getName();

    default void removeThreadLocal(){};

}
