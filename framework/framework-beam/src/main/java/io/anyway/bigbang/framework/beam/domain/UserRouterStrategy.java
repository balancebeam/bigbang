package io.anyway.bigbang.framework.beam.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
@ToString
public class UserRouterStrategy implements Serializable {
    private String id;
    private Set<ServiceWrapper> services;
    private Set<String> users;

    @Getter
    @Setter
    @ToString
    public static class ServiceWrapper {
        private String serviceId;
        private String unit;
    }

}
