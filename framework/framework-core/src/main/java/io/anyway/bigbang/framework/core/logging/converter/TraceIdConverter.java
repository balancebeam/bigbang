package io.anyway.bigbang.framework.core.logging.converter;

import io.anyway.bigbang.framework.core.logging.InheritableThreadClassicConverter;
import io.anyway.bigbang.framework.core.trace.FrameworkTraceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
public class TraceIdConverter extends InheritableThreadClassicConverter {

    @Override
    public String getInheritableThreadValue() {
        String traceId= FrameworkTraceContext.getPlatformTraceId() ;
        return (StringUtils.isEmpty(traceId)? "N/A": traceId);
    }

}
