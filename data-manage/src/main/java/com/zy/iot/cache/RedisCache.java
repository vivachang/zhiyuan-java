package com.zy.iot.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存的直接访问，基于String存取
 */
@Repository
public class RedisCache {
    @Autowired
    private StringRedisTemplate template ;
    @Autowired
    private RedisTemplate redisTemplate;

    public  void delKey(String key){
        template.delete(key);
    }

    public  void expireKey(String key,int minutes){
        template.expire(key,minutes, TimeUnit.MINUTES);
    }

    public Boolean hasKey(String key){
       return template.hasKey(key);
    }

    // String
    public  void setKey(String key,String value){
        ValueOperations<String, String> ops = template.opsForValue();
        ops.set(key,value);
    }

    public  void setKey(String key,String value,int minutes){
        ValueOperations<String, String> ops = template.opsForValue();
        ops.set(key,value,minutes, TimeUnit.MINUTES);
    }
    public String getValue(String key){
        ValueOperations<String, String> ops = this.template.opsForValue();
        return ops.get(key);
    }

    // Set
    public  void setSetKey(String key,String... value){
        SetOperations<String, String> ops = template.opsForSet();
        ops.add(key,value);
    }

    public Set<String> getSetValue(String key){
        SetOperations<String, String> ops = this.template.opsForSet();
        return ops.members(key);
    }

    public void delSetValue(String key,String... value){
        SetOperations<String, String> ops = this.template.opsForSet();
        ops.remove(key,value);
    }

    // HashMap
    public  void setHashMapfiled(String key,String filed,String value){
        HashOperations<String,String,String> ops = template.opsForHash();
        ops.put(key,filed,value);
    }
    public  void setHashMap(String key,Map<String,String> value){
        HashOperations<String,String,String> ops = template.opsForHash();
        ops.putAll(key,value);
    }

    public String getHashMapValue(String key,String filed){
        HashOperations<String,String,String>  ops = this.template.opsForHash();
        return ops.get(key,filed);
    }

    public Map<String,String> getHashMap(String key){
        HashOperations<String,String,String>  ops = this.template.opsForHash();
        return ops.entries(key);
    }

    public void delHashMapFiled(String key,String... filed){
        HashOperations<String,String,String>  ops = this.template.opsForHash();
        ops.delete(key,filed);
    }

    public void pushList(String key,Object obj){
        redisTemplate.opsForList().rightPush(key,obj);
    }

}
