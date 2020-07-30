package io.anyway.bigbang.gateway.service;

public interface ResourceStrategyService {

    void addResourceStrategy(String id, String text);

    void updateResourceStrategy(String id, String text);

    void removeResourceStrategy(String id);

}
