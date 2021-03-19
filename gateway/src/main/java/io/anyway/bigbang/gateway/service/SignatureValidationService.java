package io.anyway.bigbang.gateway.service;

import java.util.TreeMap;

public interface SignatureValidationService {

    boolean valid(String appId,String sign,String timestamp,TreeMap<String,String> content);
}
