package io.anyway.bigbang.framework.datasource.exception;

import io.anyway.bigbang.framework.core.exception.BigbangException;


public class EmptyDataSourceException extends BigbangException {

    public EmptyDataSourceException(){
        super(10000051);
    }
}
