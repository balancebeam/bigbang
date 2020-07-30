package io.anyway.bigbang.framework.beam.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class UnitRouterStrategyWrapper {
    private List<UnitRouterStrategy> inserts;
    private List<UnitRouterStrategy> updates;
    private List<UnitRouterStrategy> deletes;

}
