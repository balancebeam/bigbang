package io.anyway.bigbang.framework.beam.domain;

import com.netflix.loadbalancer.Server;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PredicateKey {
    private String serviceId;
    private String unit;
    private String version;
    private Server server;
}
