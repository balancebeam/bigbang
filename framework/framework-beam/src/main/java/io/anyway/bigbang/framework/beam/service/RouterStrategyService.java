package io.anyway.bigbang.framework.beam.service;

import com.alibaba.fastjson.JSONObject;
import io.anyway.bigbang.framework.beam.domain.UnitRouterStrategy;
import io.anyway.bigbang.framework.beam.domain.UnitRouterStrategyWrapper;
import io.anyway.bigbang.framework.beam.domain.UserRouterStrategyWrapper;
import io.anyway.bigbang.framework.beam.domain.UserRouterStrategy;
import io.anyway.bigbang.framework.core.system.InstanceConfigBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.transaction.CuratorOp;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class RouterStrategyService {

    final private static String ROOT= "/bigbang/beam";

    @Value("${zookeeper.server-list}")
    private String serverList;

    @Value("${zookeeper.session-timeout:5000}")
    private int sessionTimeout;

    @Value("${zookeeper.connection-timeout:5000}")
    private int connectionTimeout;

    @Resource
    private InstanceConfigBean bean;

    @Resource
    private Map<String, RouterStrategyCellular> routerStrategyCellularMap;

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
            for(String each: routerStrategyCellularMap.keySet()){
                String name= each.split("#")[0];
                String path = ROOT+"/"+ name;
                if(curatorFramework.checkExists().forPath(path)== null){
                    curatorFramework.create().creatingParentsIfNeeded().forPath(path);
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
            if(items.length<5 || !routerStrategyCellularMap.containsKey(items[3]+"#RouterStrategyCellular")){
                return;
            }
            log.info("Listen for changes in the path: {}, Event type: {} ",eventData.getPath(),event.getType());
            RouterStrategyCellular routerStrategyCellular= routerStrategyCellularMap.get(items[3]+"#RouterStrategyCellular");
            switch (event.getType()) {
                case NODE_ADDED:
                case NODE_UPDATED:
                    routerStrategyCellular.setRouterStrategy(items[4], new String(eventData.getData(), "UTF-8"));
                    break;
                case NODE_REMOVED:
                    routerStrategyCellular.removeRouterStrategy(items[4]);
                    break;
                default:
                    break;
            }
        });
        treeCache.start();
    }

    public void modifyWeight(int weight){
        String path= ROOT+"/weight/"+bean.getHostPort();
        try {
            curatorFramework.create().withMode(CreateMode.EPHEMERAL).forPath(path, String.valueOf(weight).getBytes("UTF-8"));
            log.info("Modify server {} weight {}", bean.getHostPort(), weight);
        } catch (Exception e) {
            log.error("Modify weight error.",e);
        }
    }

    public void shutdown(){
        String path= ROOT+"/offline/"+bean.getHostPort();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String time= sdf.format(new Date());
        try {
            if(curatorFramework.checkExists().forPath(path)==null) {
                curatorFramework.create().withMode(CreateMode.EPHEMERAL).forPath(path, time.getBytes("UTF-8"));
                log.info("shutdown server {} at {}", bean.getHostPort(), time);
            }
        } catch (Exception e) {
           log.error("Shutdown error.",e);
        }
    }

    public void setUnitStrategies(UnitRouterStrategyWrapper wrapper){
        if(wrapper==null){
            return;
        }
        try {
            List<CuratorOp> operators= new ArrayList<>();
            List<UnitRouterStrategy> list= wrapper.getInserts();
            if(list!= null && !list.isEmpty()){
                for(UnitRouterStrategy each: list) {
                    each.setId(UUID.randomUUID().toString());
                    String path = ROOT + "/unit/" + each.getId();
                    String text = JSONObject.toJSONString(each);
                    CuratorOp op= curatorFramework.transactionOp().create().withMode(CreateMode.PERSISTENT).forPath(path,text.getBytes("UTF-8"));
                    operators.add(op);
                }
            }
            if((list= wrapper.getUpdates())!= null && !list.isEmpty()){
                for(UnitRouterStrategy each: list) {
                    String path = ROOT + "/unit/" + each.getId();
                    String text = JSONObject.toJSONString(each);
                    CuratorOp op= curatorFramework.transactionOp().setData().forPath(path,text.getBytes("UTF-8"));
                    operators.add(op);
                }
            }
            if((list= wrapper.getDeletes())!= null && !list.isEmpty()){
                for(UnitRouterStrategy each: list) {
                    String path = ROOT + "/unit/" + each.getId();
                    CuratorOp op= curatorFramework.transactionOp().delete().forPath(path);
                    operators.add(op);
                }
            }
            curatorFramework.transaction().forOperations(operators);
        }catch (Exception e){
            log.error("Persist unit strategies error.",e);
        }
    }

    public void setUserStrategies(UserRouterStrategyWrapper wrapper){
        if(wrapper==null){
            return;
        }
        try {
            List<CuratorOp> operators= new ArrayList<>();
            List<UserRouterStrategy> list= wrapper.getInserts();
            if(list!= null && !list.isEmpty()){
                for(UserRouterStrategy each: list) {
                    each.setId(UUID.randomUUID().toString());
                    String path = ROOT + "/user/" + each.getId();
                    String text = JSONObject.toJSONString(each);
                    CuratorOp op= curatorFramework.transactionOp().create().withMode(CreateMode.PERSISTENT).forPath(path,text.getBytes("UTF-8"));
                    operators.add(op);
                }
            }
            if((list= wrapper.getUpdates())!= null && !list.isEmpty()){
                for(UserRouterStrategy each: list) {
                    String path = ROOT + "/user/" + each.getId();
                    String text = JSONObject.toJSONString(each);
                    CuratorOp op= curatorFramework.transactionOp().setData().forPath(path,text.getBytes("UTF-8"));
                    operators.add(op);
                }
            }
            if((list= wrapper.getDeletes())!= null && !list.isEmpty()){
                for(UserRouterStrategy each: list) {
                    String path = ROOT + "/user/" + each.getId();
                    CuratorOp op= curatorFramework.transactionOp().delete().forPath(path);
                    operators.add(op);
                }
            }
            curatorFramework.transaction().forOperations(operators);
        }catch (Exception e){
            log.error("Persist user strategies error.",e);
        }
    }

    @PreDestroy
    public void destroy(){
        shutdown();
        treeCache.close();
        curatorFramework.close();
    }
}
