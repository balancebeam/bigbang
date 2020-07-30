package io.anyway.bigbang.framework.core.logging.converter;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class LocalIPConverter extends ClassicConverter {

    private static AtomicReference<String> addressReference= new AtomicReference<>();

    private String getAddress() {
        String addr= addressReference.get();
        if(addr== null){
            try {
                addr= InetAddress.getLocalHost().getHostAddress();
            } catch (Exception e) {
                log.error("getIpAddress error",e);
            }
            if(!addressReference.compareAndSet(null,addr)){
                return getAddress();
            }
        }
        return addr;
    }

    @Override
    public String convert(ILoggingEvent event) {
        return getAddress();
    }
}
