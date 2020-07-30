package io.anyway.bigbang.framework.beam.service;


import java.util.Collection;
import java.util.Map;

public interface WeightStrategyQuerier {

    Collection<Map.Entry<String,Integer>> queryAll();

    Integer query(String hostPort);
}
