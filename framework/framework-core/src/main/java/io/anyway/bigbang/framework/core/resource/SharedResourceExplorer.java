package io.anyway.bigbang.framework.core.resource;

public interface SharedResourceExplorer<T> {

    T getResourceByName(String name);

    int getResourceSize();

    T getResourceByIndex(int index);

    void forEachResource(SharedResourceVisitor<T> visitor);
}
