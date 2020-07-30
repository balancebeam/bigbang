package io.anyway.bigbang.framework.core.logging.converter;

import io.anyway.bigbang.framework.core.client.ClientAgent;
import io.anyway.bigbang.framework.core.client.ClientAgentContextHolder;
import io.anyway.bigbang.framework.core.logging.InheritableThreadClassicConverter;
import org.springframework.util.StringUtils;

public class PlatformConverter extends InheritableThreadClassicConverter {

    @Override
    public String getInheritableThreadValue() {
        ClientAgent clientAgent= ClientAgentContextHolder.getClientAgentContext();
        if(clientAgent!= null && !StringUtils.isEmpty(clientAgent.getPlatform())){
            return clientAgent.getPlatform()+ (StringUtils.isEmpty(clientAgent.getOs())? "" : "-" + clientAgent.getOs());
        }
        return "N/A";
    }

}
