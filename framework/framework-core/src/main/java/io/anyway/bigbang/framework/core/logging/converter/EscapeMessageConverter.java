package io.anyway.bigbang.framework.core.logging.converter;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Escape message.
 *
 * @author fenggang.li
 * @since 1.20.0.0-SNAPSHOT 2019-11-22 10:14
 */
public class EscapeMessageConverter extends MessageConverter {

    @Override
    public String convert(ILoggingEvent event) {
        String message = super.convert(event);

        return EscapeMessageConverter.escapeMessage(message);
    }

    final public static String escapeMessage(String message) {
        if (message == null) {
            return null;
        }
        return message.replaceAll("\"", "`");
    }
}
