package io.anyway.bigbang.framework.core.utils;

import org.springframework.context.i18n.LocaleContextHolder;

public interface I18nUtils {

    String I18N_PREFIX = "i18n:";

    static String getMessage(String code) {
        return getMessage(code, new Object[0]);
    }

    static String getMessage(String code, String defaultMessage) {
        return getMessage(code, new Object[0], defaultMessage);
    }

    static String getMessage(String code, Object[] args) {
        return SpringUtils.getMessageSource().getMessage(code, args, LocaleContextHolder.getLocale());
    }

    static String getMessage(String code, Object[] args, String defaultMessage) {
        return SpringUtils.getMessageSource().getMessage(code, args, defaultMessage, LocaleContextHolder.getLocale());
    }

    static String getMessageByI18FullCode(String defaultMessage) {
        return I18nUtils.getMessage(defaultMessage.substring(I18nUtils.I18N_PREFIX.length()), "Parameter error");
    }
}
