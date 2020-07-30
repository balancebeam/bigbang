package io.anyway.bigbang.framework.core.trace;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class FrameworkTraceContextConfig {

    @Autowired(required = false)
    public void setFrameworkTraceContext(FrameworkTraceContext frameworkTraceContext){
        FrameworkTraceContext.INSTANCE= frameworkTraceContext;
    }

}
