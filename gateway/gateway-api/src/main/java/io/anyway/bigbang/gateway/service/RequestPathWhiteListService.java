package io.anyway.bigbang.gateway.service;

public interface RequestPathWhiteListService {
    boolean match(String path);
}
