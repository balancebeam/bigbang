package io.anyway.bigbang.framework.kernel.logging.converter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import io.anyway.bigbang.framework.kernel.logging.InheritableThreadClassicConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
public class TraceIdConverter extends InheritableThreadClassicConverter {

    @Override
    public String getInheritableThreadValue(ILoggingEvent event) {
        String traceId= "N/A";
        return (StringUtils.isEmpty(traceId)? "": traceId);
    }

}
