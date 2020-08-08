package io.anyway.bigbang.framework.cache.layeringCache.serializer;

public abstract class SerializationUtils {

    static final byte[] EMPTY_ARRAY = new byte[0];

    static boolean isEmpty(byte[] data) {
        return (data == null || data.length == 0);
    }
}
