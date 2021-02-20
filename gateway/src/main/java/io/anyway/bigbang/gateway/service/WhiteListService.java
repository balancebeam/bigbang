package io.anyway.bigbang.gateway.service;

public interface WhiteListService {
    boolean match(String path);
}
