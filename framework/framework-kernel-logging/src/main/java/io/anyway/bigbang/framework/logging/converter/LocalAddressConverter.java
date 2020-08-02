package io.anyway.bigbang.framework.logging.converter;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class LocalAddressConverter extends ClassicConverter {

    private String ip= "127.0.0.1";

    {
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String convert(ILoggingEvent event) {
        return ip;
    }
}
