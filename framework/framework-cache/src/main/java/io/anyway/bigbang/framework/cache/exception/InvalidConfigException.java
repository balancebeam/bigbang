package io.anyway.bigbang.framework.cache.exception;

import io.anyway.bigbang.framework.core.exception.BigbangException;

public class InvalidConfigException extends BigbangException {

    public InvalidConfigException(String config){
        super(config);
    }
}
