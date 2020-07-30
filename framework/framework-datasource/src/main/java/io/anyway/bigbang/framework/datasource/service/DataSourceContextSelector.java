package io.anyway.bigbang.framework.datasource.service;

import io.anyway.bigbang.framework.core.resource.SharedResourceExplorer;
import io.anyway.bigbang.framework.datasource.domain.DataSourceWrapper;

public interface DataSourceContextSelector {

    DataSourceWrapper choose(SharedResourceExplorer<DataSourceWrapper> explorer);
}
