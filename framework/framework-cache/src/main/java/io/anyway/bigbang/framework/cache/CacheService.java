package io.anyway.bigbang.framework.cache;

import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

public interface CacheService {

    void set(String key, String value);

    void set(String key, String value, long timeout);

    String get(String key);

    boolean delete(String key);

    Long delete(Collection<String> keys);

    boolean hasKey(String key);

    boolean expire(String key, long timeout);

    long getExpire(final String key);

    boolean setNX(String key, String value);

    boolean setNX(String key, String value, long timeout);

    boolean setXX(String key, String value, long timeout);



}
