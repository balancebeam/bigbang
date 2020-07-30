package io.anyway.bigbang.framework.cache.distributedlock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

public interface LockService {

    Lock getLock(String name);

    ReadWriteLock getReadWriteLock(String name);

}
