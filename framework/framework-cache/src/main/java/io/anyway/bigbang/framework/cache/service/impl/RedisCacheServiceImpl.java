package io.anyway.bigbang.framework.cache.service.impl;

import io.anyway.bigbang.framework.cache.CacheService;
import io.anyway.bigbang.framework.cache.service.CacheKeyWrapperProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class RedisCacheServiceImpl implements CacheService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Resource
    private CacheKeyWrapperProcessor cacheKeyWrapperProcessor;

    private final byte[] rawKey(final String key) {
        String wrapperKey= cacheKeyWrapperProcessor.process(key);
        return redisTemplate.getKeySerializer().serialize(wrapperKey);
    }

    private final byte[] encodeVal(String value) {
        return redisTemplate.getValueSerializer().serialize(value);
    }

    private final String decodeVal(byte[] value){
        return (String)redisTemplate.getValueSerializer().deserialize(value);
    }

    private final byte[][] rawKeys(Collection<String> keys) {
        final byte[][] rawKeys = new byte[keys.size()][];
        int i = 0;
        for (String key : keys) {
            rawKeys[i++] = rawKey(key);
        }
        return rawKeys;
    }

    @Override
    public void set(String key, String value) {
        redisTemplate.execute((RedisCallback) conn -> conn.set(rawKey(key), encodeVal(value)));
    }

    @Override
    public void set(String key, String value, long timeout) {
        redisTemplate.execute((RedisCallback) conn -> conn.setEx(rawKey(key),timeout,encodeVal(value)));
    }

    @Override
    public String get(String key) {
        return decodeVal((byte[])redisTemplate.execute((RedisCallback) conn -> conn.get(rawKey(key))));
    }

    @Override
    public boolean delete(final String key) {
        return 1==(Long)redisTemplate.execute((RedisCallback) conn -> conn.del(rawKey(key)));
    }

    @Override
    public Long delete(final Collection<String> keys) {
        return (Long)redisTemplate.execute((RedisCallback) conn -> conn.del(rawKeys(keys)));
    }

    @Override
    public boolean hasKey(String key) {
        return (Boolean) redisTemplate.execute((RedisCallback) conn -> conn.exists(rawKey(key)));
    }

    @Override
    public boolean expire(String key, long timeout) {
        return (Boolean) redisTemplate.execute((RedisCallback) conn -> conn.expire(rawKey(key), timeout));
    }

    @Override
    public long getExpire(final String key) {
        return (Long) redisTemplate.execute((RedisCallback) conn -> conn.ttl(rawKey(key)));
    }

    @Override
    public boolean setNX(String key, String value) {
        return (Boolean) redisTemplate.execute((RedisCallback) conn -> conn.setNX(rawKey(key), encodeVal(value)));
    }
    /**
     * set key value [EX seconds] [PX milliseconds] [NX|XX]
     * EX seconds: timeout seconds number
     * PX milliseconds: timeout milliseconds number
     * NX：if key was absent then set value and return Ok, otherwise return nil
     * XX：if key was present then set value and return Ok, otherwise return nil key
     **/
    @Override
    public boolean setNX(String key, String value, long timeout) {
        return (Boolean)redisTemplate.execute((RedisCallback) conn ->
                Boolean.TRUE.equals(conn.set(rawKey(key),
                        encodeVal(value),
                        Expiration.milliseconds(timeout),
                        RedisStringCommands.SetOption.SET_IF_ABSENT))
        );
    }

    @Override
    public boolean setXX(String key, String value, long timeout) {
        return (Boolean)redisTemplate.execute((RedisCallback) conn ->
                Boolean.TRUE.equals(conn.set(rawKey(key),
                        encodeVal(value),
                        Expiration.milliseconds(timeout),
                        RedisStringCommands.SetOption.SET_IF_PRESENT))
        );
    }

    public Object eval(String script, ReturnType returnType, List<String> keys, List<Object> values){
        return redisTemplate.execute((RedisCallback) conn ->{
            List<byte[]> keysAndArgs= new ArrayList<>();
            keys.forEach(key->keysAndArgs.add(rawKey(key)));
            values.forEach(value->keysAndArgs.add(redisTemplate.getStringSerializer().serialize(value+"")));
            return conn.eval(
                    redisTemplate.getKeySerializer().serialize(script),
                    returnType,
                    keys.size(),
                    keysAndArgs.toArray(new byte[][]{})
            );
        });
    }
}
