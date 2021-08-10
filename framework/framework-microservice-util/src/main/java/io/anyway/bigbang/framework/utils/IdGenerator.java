package io.anyway.bigbang.framework.utils;


import java.util.UUID;

public class IdGenerator {

    final private static IdWorker idWorker=
            new IdWorker(System.identityHashCode(UUID.randomUUID().toString()));

    public static long next() {
        return idWorker.getId();
    }
}
