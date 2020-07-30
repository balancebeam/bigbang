package io.anyway.bigbang.framework.beam.service;

import io.anyway.bigbang.framework.beam.domain.PredicateKey;

public interface NodeRouterPredicate {

    boolean apply(PredicateKey key);
}
