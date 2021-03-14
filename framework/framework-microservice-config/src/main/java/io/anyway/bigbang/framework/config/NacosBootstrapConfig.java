package io.anyway.bigbang.framework.config;

import com.alibaba.cloud.nacos.NacosConfigBootstrapConfiguration;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;

@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "spring.cloud.nacos.config.extension-multi-version",name="enabled",havingValue = "true",matchIfMissing = true)
@AutoConfigureBefore(NacosConfigBootstrapConfiguration.class)
public class NacosBootstrapConfig {

    @Resource
    private Environment environment;

    @Bean
    public NacosConfigProperties nacosConfigProperties() {
        return new NacosConfigProperties();
    }

    @Bean
    public NacosConfigManager nacosConfigManager2(
            NacosConfigProperties nacosConfigProperties) {

        List<NacosConfigProperties.Config> configs= nacosConfigProperties.getExtensionConfigs();
        if(!CollectionUtils.isEmpty(configs) &&
                environment.getActiveProfiles().length>0){
            for(int i=0,j= configs.size();i<j;i++){
                NacosConfigProperties.Config config= configs.get(i);
                String dataId= config.getDataId();
                String fileExtension= nacosConfigProperties.getFileExtension();
                int index= dataId.lastIndexOf(".");
                if(index>0){
                    fileExtension= dataId.substring(index+1);
                    dataId= dataId.substring(0,index);
                }
                else{
                    NacosConfigProperties.Config newConfig= new NacosConfigProperties.Config();
                    newConfig.setDataId(dataId+"."+fileExtension);
                    newConfig.setGroup(config.getGroup());
                    newConfig.setRefresh(config.isRefresh());
                    configs.add(newConfig);
                }
                for(String each: environment.getActiveProfiles()){
                    NacosConfigProperties.Config newConfig= new NacosConfigProperties.Config();
                    newConfig.setDataId(dataId+"-"+each+"."+fileExtension);
                    newConfig.setGroup(config.getGroup());
                    newConfig.setRefresh(config.isRefresh());
                    configs.add(newConfig);
                }
            }


//            List<ExtNacosConfig> extNacosConfigList= new ArrayList<>();
//            for(int i=0;i<extConfigSize;i++){
//                NacosConfigProperties.Config config= configs.get(i);
//                ExtNacosConfig extNacosConfig= new ExtNacosConfig();
//                extNacosConfig.setDataId(config.getDataId());
//                extNacosConfig.setGroup(config.getGroup());
//                extNacosConfig.setRefresh(config.isRefresh());
//                extNacosConfig.setTargetDataId(config.getDataId());
//                extNacosConfigList.add(extNacosConfig);
//            }
//            for(int i=0;i<extConfigSize;i++){
//                for(String each: environment.getActiveProfiles()){
//                    ExtNacosConfig config= extNacosConfigList.get(i);
//                    ExtNacosConfig newConfig= new ExtNacosConfig();
//                    String dataId= config.getDataId();
//                    int index= dataId.lastIndexOf(".");
//                    if(index>0){
//                        String fileExtension= dataId.substring(index+1);
//                        dataId= dataId.substring(0,index);
//                        newConfig.setDataId(dataId+"-"+each+"."+fileExtension);
//                    }
//                    else{
//                        newConfig.setDataId(dataId+"-"+each);
//                    }
//                    newConfig.setGroup(config.getGroup());
//                    newConfig.setRefresh(config.isRefresh());
//                    newConfig.setTargetDataId(config.getTargetDataId());
//                    extNacosConfigList.add(newConfig);
//                }
//            }
//            configs.clear();
//            configs.addAll(extNacosConfigList);
            log.info("extension multiple version config list: {}", configs);
        }
        return new NacosConfigManager(nacosConfigProperties);
    }
}
