package io.anyway.bigbang.framework.core.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public interface LoggingLevelExchange {

    static boolean exchange(String name, String level) {
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(level)) {
            return false;
        }
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = loggerContext.getLogger(name);
        if (logger != null) {
            logger.setLevel(Level.toLevel(level));
            return true;
        }
        return false;
    }

}
