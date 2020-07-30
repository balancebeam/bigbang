package io.anyway.bigbang.framework.cache.service.impl;

import com.alibaba.fastjson.JSONObject;
import io.anyway.bigbang.framework.cache.exception.InvalidConfigException;
import io.anyway.bigbang.framework.cache.service.RedisConnectionFactoryBuilder;
import io.anyway.bigbang.framework.cache.property.XRedisProperties;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class RedissonConnectionFactoryBuilder implements RedisConnectionFactoryBuilder<RedissonConnectionFactory> {

    @Autowired
    private ApplicationContext ctx;

    @Override
    public RedissonConnectionFactory build(XRedisProperties redisProperties) {
        try {
            RedissonConnectionFactory redissonConnectionFactory= new RedissonConnectionFactory(redisson(redisProperties));
            redissonConnectionFactory.afterPropertiesSet();
            return redissonConnectionFactory;
        } catch (Exception e) {
            log.error("Init redisson error",e);
            throw new InvalidConfigException(JSONObject.toJSONString(redisProperties));
        }
    }

    @Override
    public void destroy(RedissonConnectionFactory f) {
        try {
            f.destroy();
        } catch (Exception e) {
            log.error("Destroy RedissonConnectionFactory error :{}",f,e);
        }
    }

    private RedissonClient redisson(XRedisProperties redisProperties) throws IOException {
        Config config = null;
        Method clusterMethod = ReflectionUtils.findMethod(XRedisProperties.class, "getCluster");
        Method timeoutMethod = ReflectionUtils.findMethod(XRedisProperties.class, "getTimeout");
        Object timeoutValue = ReflectionUtils.invokeMethod(timeoutMethod, redisProperties);
        int timeout;
        if(null == timeoutValue){
            timeout = 10000;
        }else if (!(timeoutValue instanceof Integer)) {
            Method millisMethod = ReflectionUtils.findMethod(timeoutValue.getClass(), "toMillis");
            timeout = ((Long) ReflectionUtils.invokeMethod(millisMethod, timeoutValue)).intValue();
        } else {
            timeout = (Integer)timeoutValue;
        }

        if (redisProperties.getRedissonConfig() != null) {
            try {
                InputStream is = getConfigStream(redisProperties);
                config = Config.fromJSON(is);
            } catch (IOException e) {
                // trying next format
                try {
                    InputStream is = getConfigStream(redisProperties);
                    config = Config.fromYAML(is);
                } catch (IOException e1) {
                    throw new IllegalArgumentException("Can't parse config", e1);
                }
            }
        } else if (redisProperties.getSentinel() != null) {
            Method nodesMethod = ReflectionUtils.findMethod(XRedisProperties.Sentinel.class, "getNodes");
            Object nodesValue = ReflectionUtils.invokeMethod(nodesMethod, redisProperties.getSentinel());

            String[] nodes;
            if (nodesValue instanceof String) {
                nodes = convert(Arrays.asList(((String)nodesValue).split(",")));
            } else {
                nodes = convert((List<String>)nodesValue);
            }

            config = new Config();
            config.useSentinelServers()
                    .setMasterName(redisProperties.getSentinel().getMaster())
                    .addSentinelAddress(nodes)
                    .setDatabase(redisProperties.getDatabase())
                    .setConnectTimeout(timeout)
                    .setPassword(redisProperties.getPassword());
        } else if (clusterMethod != null && ReflectionUtils.invokeMethod(clusterMethod, redisProperties) != null) {
            Object clusterObject = ReflectionUtils.invokeMethod(clusterMethod, redisProperties);
            Method nodesMethod = ReflectionUtils.findMethod(clusterObject.getClass(), "getNodes");
            List<String> nodesObject = (List) ReflectionUtils.invokeMethod(nodesMethod, clusterObject);

            String[] nodes = convert(nodesObject);

            config = new Config();
            config.useClusterServers()
                    .addNodeAddress(nodes)
                    .setConnectTimeout(timeout)
                    .setPassword(redisProperties.getPassword());
        } else {
            config = new Config();
            String prefix = "redis://";
            Method method = ReflectionUtils.findMethod(XRedisProperties.class, "isSsl");
            if (method != null && (Boolean)ReflectionUtils.invokeMethod(method, redisProperties)) {
                prefix = "rediss://";
            }

            config.useSingleServer()
                    .setAddress(prefix + redisProperties.getHost() + ":" + redisProperties.getPort())
                    .setConnectTimeout(timeout)
                    .setDatabase(redisProperties.getDatabase())
                    .setPassword(redisProperties.getPassword());
        }

        return Redisson.create(config);
    }

    private String[] convert(List<String> nodesObject) {
        List<String> nodes = new ArrayList<String>(nodesObject.size());
        for (String node : nodesObject) {
            if (!node.startsWith("redis://") && !node.startsWith("rediss://")) {
                nodes.add("redis://" + node);
            } else {
                nodes.add(node);
            }
        }
        return nodes.toArray(new String[nodes.size()]);
    }

    private InputStream getConfigStream(XRedisProperties redisProperties) throws IOException {
        Resource resource = ctx.getResource(redisProperties.getRedissonConfig());
        InputStream is = resource.getInputStream();
        return is;
    }
}
