package io.anyway.bigbang.framework.beam.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class UserRouterStrategyWrapper {
    private List<UserRouterStrategy> inserts;
    private List<UserRouterStrategy> updates;
    private List<UserRouterStrategy> deletes;
}
