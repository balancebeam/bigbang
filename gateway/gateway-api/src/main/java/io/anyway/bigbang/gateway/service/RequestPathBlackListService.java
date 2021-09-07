package io.anyway.bigbang.gateway.service;

public interface RequestPathBlackListService {
    boolean match(String path);
}
