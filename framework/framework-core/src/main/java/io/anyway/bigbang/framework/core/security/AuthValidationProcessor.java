package io.anyway.bigbang.framework.core.security;

import java.util.Map;

public interface AuthValidationProcessor {

    void valid(Map<String,Object> map) throws Exception;
}
