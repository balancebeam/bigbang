package io.anyway.bigbang.framework.core.client;

import io.anyway.bigbang.framework.core.concurrent.InheritableThreadProcessor;
import io.anyway.bigbang.framework.core.interceptor.HeaderDeliveryInterceptor;
import io.anyway.bigbang.framework.core.security.SecurityContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

@Slf4j
@Configuration
public class ClientAgentConfig {

    @Bean
    public InheritableThreadProcessor clientAgentInheritableThreadProcessor(){
        log.debug("init clientAgentInheritableThreadProcessor");
        return new InheritableThreadProcessor<ClientAgent>() {

            @Override
            public ClientAgent getInheritableThreadValue() {
                return ClientAgentContextHolder.getClientAgentContext();
            }

            @Override
            public void setInheritableThreadValue(ClientAgent clientAgent) {
                ClientAgentContextHolder.setClientAgentContext(clientAgent);
            }

            @Override
            public void removeInheritableThreadValue() {
                SecurityContextHolder.remove();
            }

        };
    }

    @Bean
    public InheritableThreadProcessor localeInheritableThreadProcessor(){
        log.info("init localeInheritableThreadProcessor");
        return new InheritableThreadProcessor<Locale>() {

            @Override
            public Locale getInheritableThreadValue() {
                return LocaleContextHolder.getLocale();
            }

            @Override
            public void setInheritableThreadValue(Locale locale) {
                LocaleContextHolder.setLocale(locale);
            }

            @Override
            public void removeInheritableThreadValue() {
                LocaleContextHolder.setLocale(null);
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(name="localeHeaderDeliveryInterceptor")
    public HeaderDeliveryInterceptor localeHeaderDeliveryInterceptor(){
        log.info("init LocaleHeaderDeliveryInterceptor");
        return headers -> {
            headers.put("Accept-Language", LocaleContextHolder.getLocale().getLanguage());
        };
    }

    @Bean
    @ConditionalOnMissingBean(name="localeHeaderDeliveryInterceptor")
    public HeaderDeliveryInterceptor clientAgentHeaderDeliveryInterceptor(){
        log.info("init ClientAgentHeaderDeliveryInterceptor");
        return headers -> {
            if(ClientAgentContextHolder.getClientAgentContext()!= null) {
                headers.put("X-Client-Agent", ClientAgentContextHolder.getClientAgentContext().toJson());
            }
        };
    }


}
