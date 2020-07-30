package io.anyway.bigbang.framework.datasource.exception;

import io.anyway.bigbang.framework.core.exception.BigbangException;


public class NoFoundDataSourceException extends BigbangException {

    public NoFoundDataSourceException(String name){
        super(10000050,new Object[]{name});
    }
}
