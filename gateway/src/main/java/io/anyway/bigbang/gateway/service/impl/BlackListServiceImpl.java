package io.anyway.bigbang.gateway.service.impl;

import io.anyway.bigbang.gateway.service.BlackListService;
import org.springframework.stereotype.Service;

@Service
public class BlackListServiceImpl implements BlackListService {

    @Override
    public boolean match(String path) {
        return false;
    }
}
