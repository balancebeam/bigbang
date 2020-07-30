package io.anyway.bigbang.framework.beam.service;

import io.anyway.bigbang.framework.beam.domain.UnitRouterStrategy;

import java.util.Collection;

public interface UnitStrategyQuerier {

   Collection<UnitRouterStrategy> queryAll();

   UnitRouterStrategy query(String id);
}
