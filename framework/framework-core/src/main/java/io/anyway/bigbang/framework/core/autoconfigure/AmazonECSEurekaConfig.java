package io.anyway.bigbang.framework.core.autoconfigure;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.netflix.appinfo.AmazonInfo;
import io.anyway.bigbang.framework.core.condition.ConditionalOnAmazonECS;
import io.anyway.bigbang.framework.core.system.InstanceConfigBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;

@Slf4j
@AutoConfigureBefore(PlatformEnvironmentConfiguration.class)
@ConditionalOnClass(EurekaInstanceConfigBean.class)
public class AmazonECSEurekaConfig {

    @Resource
    private InetUtils inetUtils;

    @Value("${server.port:8000}")
    private int port;

    @Bean
    @ConditionalOnAmazonECS
    public EurekaInstanceConfigBean ecsEurekaInstanceConfig() {
        EurekaInstanceConfigBean eurekaInstanceConfigBean = new EurekaInstanceConfigBean(inetUtils);
        AmazonInfo amazonInfo = AmazonInfo.Builder.newBuilder().autoBuild("eureka");
        eurekaInstanceConfigBean.setDataCenterInfo(amazonInfo);
        log.debug("metadata: {}",amazonInfo.getMetadata());
        String hostIp= amazonInfo.get(AmazonInfo.MetaDataKey.localIpv4);
        int port = getHostPort(hostIp);
        amazonInfo.getMetadata().put(AmazonInfo.MetaDataKey.localHostname.getName(), hostIp);
        amazonInfo.getMetadata().put(
                AmazonInfo.MetaDataKey.instanceId.getName(),
                amazonInfo.getMetadata().get(AmazonInfo.MetaDataKey.instanceId.getName()) + ":" + port);
        eurekaInstanceConfigBean.setHostname(amazonInfo.get(AmazonInfo.MetaDataKey.localHostname));
        eurekaInstanceConfigBean.setIpAddress(hostIp);
        eurekaInstanceConfigBean.setNonSecurePort(port);
        log.info("EurekaInstanceConfigBean: {}",eurekaInstanceConfigBean);
        return eurekaInstanceConfigBean;
    }

    @Bean
    public InstanceConfigBean createInstanceConfigBeanFromEureka(final EurekaInstanceConfigBean configBean){
        InstanceConfigBean instanceConfigBean= new InstanceConfigBean();
        instanceConfigBean.setHost(configBean.getIpAddress());
        instanceConfigBean.setPort(configBean.getNonSecurePort());
        return instanceConfigBean;
    }

    private int getHostPort(String hostIp){
        try {
            String dockerId= InetAddress.getLocalHost().getHostName();
            if(StringUtils.isEmpty(dockerId)){
                return port;
            }
            String taskURL= "http://"+hostIp+":51678/v1/tasks?dockerid="+dockerId;
            log.debug("docker taskURL: {}",taskURL);
            URL url= new URL(taskURL);

            URLConnection conn= url.openConnection();
            conn.setConnectTimeout(1000);
            conn.connect();
            try(InputStream in=  conn.getInputStream()) {
                if(in!= null) {
                    int length = in.available();
                    byte[] b = new byte[length];
                    in.read(b);
                    String text = new String(b,"UTF-8");
                    log.debug("Task content: {}", text);
                    JSONObject task = JSONObject.parseObject(text);
                    JSONArray containers = task.getJSONArray("Containers");
                    for (int k = 0; k < containers.size(); k++) {
                        JSONObject item = containers.getJSONObject(k);
                        JSONArray ports = item.getJSONArray("Ports");
                        JSONObject port = ports.getJSONObject(0);
                        log.debug("Port node: {}", port);
                        return port.getIntValue("HostPort");
                    }
                }
            }

        } catch (Exception e) {
            log.warn("read the url error,use default port: {}, cause: {}",port,e.getMessage());
        }
        return port;
    }

}