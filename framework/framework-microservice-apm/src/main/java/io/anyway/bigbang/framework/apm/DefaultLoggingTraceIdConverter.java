package io.anyway.bigbang.framework.apm;

import ch.qos.logback.classic.spi.ILoggingEvent;
import io.anyway.bigbang.framework.apm.config.LocalTraceConfigure;
import io.anyway.bigbang.framework.header.HeaderContextHolder;
import io.anyway.bigbang.framework.logging.InheritableThreadClassicConverter;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class DefaultLoggingTraceIdConverter extends InheritableThreadClassicConverter {

    @Override
    public String getInheritableThreadValue(ILoggingEvent event) {
        Optional<String> optional= HeaderContextHolder.getHeaderValue(LocalTraceConfigure.TRACE_HEADER_NAME);
        return optional.isPresent()? optional.get(): "";
    }

}
