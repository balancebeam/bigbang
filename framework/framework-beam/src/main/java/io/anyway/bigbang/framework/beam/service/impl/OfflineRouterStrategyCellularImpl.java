package io.anyway.bigbang.framework.beam.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.anyway.bigbang.framework.beam.service.OfflinePredictService;
import io.anyway.bigbang.framework.beam.service.RouterStrategyCellular;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class OfflineRouterStrategyCellularImpl implements RouterStrategyCellular, OfflinePredictService {

    private Cache<String,Integer> offlineList = CacheBuilder.newBuilder()
            .expireAfterWrite(2L, TimeUnit.MINUTES)
            .concurrencyLevel(6)
            .initialCapacity(10)
            .maximumSize(200)
            .softValues()
            .build();

    @Override
    public void setRouterStrategy(String hostPost, String text) {
        log.debug("The server {} will be in offline",hostPost);
        offlineList.put(hostPost,1);
    }

    @Override
    public void removeRouterStrategy(String id) {
    }

    @Override
    public boolean isOffline(String hostPort) {
        return offlineList.getIfPresent(hostPort)!= null;
    }
}
