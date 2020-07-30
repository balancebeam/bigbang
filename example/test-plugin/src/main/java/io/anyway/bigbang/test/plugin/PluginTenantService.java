package io.anyway.bigbang.test.plugin;

import io.anyway.bigbang.test.ThisTenantService;
import io.anyway.bigbang.test.plugin.dao.PluginDao;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

public class PluginTenantService implements ThisTenantService {

    @Resource
    private PluginDao pluginDao;

    @Override
    @Transactional
    public String hello() {
        return pluginDao.count()+"";
    }
}
