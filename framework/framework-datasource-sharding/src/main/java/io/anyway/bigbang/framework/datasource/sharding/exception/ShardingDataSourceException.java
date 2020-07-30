package io.anyway.bigbang.framework.datasource.sharding.exception;

import io.anyway.bigbang.framework.core.exception.BigbangException;

public class ShardingDataSourceException extends BigbangException {
    public ShardingDataSourceException(){
        super(10000052);
    }
}
