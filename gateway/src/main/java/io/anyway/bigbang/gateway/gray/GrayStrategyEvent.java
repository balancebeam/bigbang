package io.anyway.bigbang.gateway.gray;

import org.springframework.context.ApplicationEvent;

public class GrayStrategyEvent extends ApplicationEvent {
    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     */
    public GrayStrategyEvent(Object source) {
        super(source);
    }
}
