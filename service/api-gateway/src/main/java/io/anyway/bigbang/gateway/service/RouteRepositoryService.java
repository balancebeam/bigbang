package io.anyway.bigbang.gateway.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@Service
public class RouteRepositoryService {

    @Value("${bigbang.gateway.root:/bigbang/gateway}")
    private String ROOT;

    @Value("${zookeeper.server-list}")
    private String serverList;

    @Value("${zookeeper.session-timeout:5000}")
    private int sessionTimeout;

    @Value("${zookeeper.connection-timeout:5000}")
    private int connectionTimeout;

    @Resource
    private Map<String,ResourceStrategyService> resourceStrategyServiceMap;

    private CuratorFramework curatorFramework ;

    private TreeCache treeCache;

    @PostConstruct
    public void init(){
        RetryPolicy retryPolicy  = new ExponentialBackoffRetry(1000,3);
        curatorFramework= CuratorFrameworkFactory.builder()
                .connectString(serverList)
                .sessionTimeoutMs(sessionTimeout)
                .connectionTimeoutMs(connectionTimeout)
                .retryPolicy(retryPolicy)
                .build();
        curatorFramework.start();
        //init path
        try {
            for(String each: resourceStrategyServiceMap.keySet()){
                String name= each.split("#")[0];
                String path = ROOT+"/"+ name;
                if(curatorFramework.checkExists().forPath(path)== null){
                    curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                }
            }
        }catch (Exception e){
            log.error("Initialize path error",e);
        }
        //add node listener
        try {
            addListener();
        }catch (Exception e){
            log.error("Startup TreeCache error",e);
        }
    }

    private void addListener()throws Exception{
        treeCache = new TreeCache(curatorFramework, ROOT);
        treeCache.getListenable().addListener((client, event) -> {
            ChildData eventData = event.getData();
            if(eventData==null){
                return;
            }
            String[] items= eventData.getPath().split("/");
            if(items.length<5 || !resourceStrategyServiceMap.containsKey(items[3]+"#ResourceStrategyService")){
                return;
            }
            log.info("Listen for changes in the path: {}, Event type: {} ",eventData.getPath(),event.getType());
            ResourceStrategyService resourceStrategyService= resourceStrategyServiceMap.get(items[3]+"#ResourceStrategyService");
            switch (event.getType()) {
                case NODE_ADDED:
                    resourceStrategyService.addResourceStrategy(items[4],new String(eventData.getData(),"UTF-8"));
                    break;
                case NODE_UPDATED:
                    resourceStrategyService.updateResourceStrategy(items[4],new String(eventData.getData(),"UTF-8"));
                    break;
                case NODE_REMOVED:
                    resourceStrategyService.removeResourceStrategy(items[4]);
                    break;
                default:
                    break;
            }
        });
        treeCache.start();
    }

    @PreDestroy
    public void destroy(){
        treeCache.close();
        curatorFramework.close();
    }
}
