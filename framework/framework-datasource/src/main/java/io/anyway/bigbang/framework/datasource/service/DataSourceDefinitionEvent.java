package io.anyway.bigbang.framework.datasource.service;

import io.anyway.bigbang.framework.core.resource.SharedResourceExplorer;
import io.anyway.bigbang.framework.datasource.domain.DataSourceWrapper;

public interface DataSourceDefinitionEvent {

    void onAdd(DataSourceWrapper target);

    void onRemove(DataSourceWrapper target);

    void onInit(SharedResourceExplorer<DataSourceWrapper> explorer);

}
