package io.anyway.bigbang.framework.core.condition;

import com.alibaba.fastjson.JSONObject;
import com.netflix.appinfo.AmazonInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
class OnAmazonECSCondition implements Condition {

    private static AtomicInteger result= new AtomicInteger(-1);

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        //prevent to execute duplicated match method
        if(result.get()!= -1){
            return result.get()==1;
        }
        try {
            AmazonInfo amazonInfo = AmazonInfo.Builder.newBuilder().autoBuild("eureka");
            log.debug("query eureka metadata: {}",amazonInfo.getMetadata());
            String hostIp= amazonInfo.get(AmazonInfo.MetaDataKey.localIpv4);
            String path= "http://"+hostIp+":51678/v1/metadata";
            log.debug("Amazon ECS metadata url: {}",path);
            URL url = new URL(path);
            URLConnection conn= url.openConnection();
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(1000);
            try(InputStream in= conn.getInputStream()) {
                if (in != null) {
                    byte[] b = new byte[in.available()];
                    in.read(b);
                    String txt = new String(b,"UTF-8");
                    log.debug("Amazon ECS Container metadata: {}", txt);
                    JSONObject json = JSONObject.parseObject(txt);
                    if (json != null) {
                        if (json.getString("Cluster") != null) {
                            log.debug("application is in Amazon ECS evn.");
                            result.compareAndSet(-1,1);
                            return true;
                        }
                    }
                }
            }
        }catch (Exception e){
            log.debug("query Amazon ECS Container error: {}",e.getMessage());
        }
        log.debug("application is not in Amazon ECS evn.");
        result.compareAndSet(-1,0);
        return false;
    }
}


