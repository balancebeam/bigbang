package io.anyway.bigbang.gateway.service;

public interface ApiPrivilegeValidatorService {

    boolean permit(String serviceCode);
}
