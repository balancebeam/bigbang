package io.anyway.bigbang.framework.core.concurrent;

public interface InheritableThreadProcessor<T> {

    T getInheritableThreadValue();

    void setInheritableThreadValue(T value);

    void removeInheritableThreadValue();
}
