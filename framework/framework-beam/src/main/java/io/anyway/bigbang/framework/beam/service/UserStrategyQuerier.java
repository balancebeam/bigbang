package io.anyway.bigbang.framework.beam.service;

import io.anyway.bigbang.framework.beam.domain.UserRouterStrategy;

import java.util.Collection;


public interface UserStrategyQuerier {

    Collection<UserRouterStrategy> queryAll();

    UserRouterStrategy query(String id);
}
