package io.anyway.bigbang.framework.core.logging.converter;

import io.anyway.bigbang.framework.core.client.ClientAgent;
import io.anyway.bigbang.framework.core.client.ClientAgentContextHolder;
import io.anyway.bigbang.framework.core.logging.InheritableThreadClassicConverter;
import org.springframework.util.StringUtils;

public class VersionConverter extends InheritableThreadClassicConverter {

    @Override
    public String getInheritableThreadValue() {
        ClientAgent clientAgent= ClientAgentContextHolder.getClientAgentContext();
        if(clientAgent!= null && !StringUtils.isEmpty(clientAgent.getVersion())){
            return clientAgent.getVersion();
        }
        return "N/A";
    }
}
