package io.anyway.bigbang.test.plugin.dao;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PluginDao {

    int count();
}
