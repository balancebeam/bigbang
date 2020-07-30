package io.anyway.bigbang.framework.beam.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class WeightRouterStrategy {
    private String hostPort;
    private int weight;
}
