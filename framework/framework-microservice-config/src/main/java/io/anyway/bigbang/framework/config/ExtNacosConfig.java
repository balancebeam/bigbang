package io.anyway.bigbang.framework.config;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.cloud.nacos.client.NacosPropertySourceLocator;
import lombok.Getter;
import lombok.Setter;

public class ExtNacosConfig extends NacosConfigProperties.Config{

    int callNumber= 0;

    @Getter
    @Setter
    private String targetDataId;

    @Override
    public String getDataId(){
        StackTraceElement stackTraceElement= Thread.currentThread().getStackTrace()[2];
        if(NacosPropertySourceLocator.class.getName().equals(stackTraceElement.getClassName())
            && "loadNacosConfiguration".equals(stackTraceElement.getMethodName())){
            if(++callNumber==2) {
                return targetDataId;
            }
        }
        return super.getDataId();
    }
}
