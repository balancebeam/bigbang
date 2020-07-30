package io.anyway.bigbang.framework.core.utils;

public interface XStringUtils {

    static int toHash(String text) {
        int arraySize = 11113;
        int hashCode = 0;
        for (int i = 0; i < text.length(); i++) {
            int letterValue = text.charAt(i) - 96;
            hashCode = ((hashCode << 5) + letterValue) % arraySize;
        }
        return hashCode;
    }
}
