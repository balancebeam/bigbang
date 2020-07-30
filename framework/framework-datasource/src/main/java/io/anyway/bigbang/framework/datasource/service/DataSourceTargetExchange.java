package io.anyway.bigbang.framework.datasource.service;


import io.anyway.bigbang.framework.datasource.domain.DataSourceWrapper;

import javax.sql.DataSource;

public interface DataSourceTargetExchange {

    DataSource mutate(DataSourceWrapper ds);
}
