package io.anyway.bigbang.framework.kernel.header;

public interface PrincipleHeaderKey {

    String name();

    default void removeThreadLocal(){};

}
