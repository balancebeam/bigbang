package io.anyway.bigbang.framework.core.resource;

public interface SharedResourceVisitor<T> {

    void visit(T resource);
}
