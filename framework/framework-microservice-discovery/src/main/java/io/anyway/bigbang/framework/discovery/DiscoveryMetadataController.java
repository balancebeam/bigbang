package io.anyway.bigbang.framework.discovery;

import io.anyway.bigbang.framework.model.api.ApiResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Slf4j
@RequestMapping("/system/discovery")
public class DiscoveryMetadataController {

    @Resource
    private DiscoveryMetadataService metadata;

    @ResponseBody
    @GetMapping("/metadata")
    public ApiResponseEntity<DiscoveryMetadataService> info() throws Exception{
        return ApiResponseEntity.ok(metadata);
    }
}

