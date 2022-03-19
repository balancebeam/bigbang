package io.anyway.bigbang.framework.utils;


import java.util.UUID;

public class IdGenerator {

    final private static IdWorker idWorker=
            new IdWorker(System.identityHashCode(UUID.randomUUID().toString()));

    public static long next() {
        return idWorker.getId();
    }

    public static String nextRadixId() {
        return idWorker.getRadixId();
    }

    public static String nextRadixId(String prefix) {
        return new StringBuilder().append(prefix).append(nextRadixId()).toString();
    }
}
