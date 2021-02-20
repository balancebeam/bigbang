package io.anyway.bigbang.gateway.service;

public interface BlackListService {
    boolean match(String path);
}
