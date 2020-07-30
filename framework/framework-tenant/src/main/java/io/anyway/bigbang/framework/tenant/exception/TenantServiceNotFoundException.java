package io.anyway.bigbang.framework.tenant.exception;

import io.anyway.bigbang.framework.core.exception.BigbangException;

public class TenantServiceNotFoundException extends BigbangException {

    public TenantServiceNotFoundException(String msg){
        super(msg);
    }
}
