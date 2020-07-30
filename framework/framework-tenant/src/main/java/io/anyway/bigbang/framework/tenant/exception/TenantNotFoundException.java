package io.anyway.bigbang.framework.tenant.exception;

import io.anyway.bigbang.framework.core.exception.BigbangException;

public class TenantNotFoundException extends BigbangException {

    public TenantNotFoundException(){
        super(10120001);
    }
}
