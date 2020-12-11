package com.forest.dataacquisitionserver.session;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionDistributedManager {
    private static final String ONLINE_CLIENT_REDIS_PREFIX = "zhiyuan_database_iot:client:online";
    private final Gson gson;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<String> getAndRemoveScript;
    private final RedisScript<Boolean> removeIfScript;
    private final RedisScript<Boolean> setAndExpireScript;
    private final RedisScript<String> getAndExpireScript;

    public boolean put(String clientId, SessionDistributed session) {
        String sessionStr = gson.toJson(session, SessionDistributed.class);
        String expire = String.valueOf(Math.round(session.getExpire()*2.0f));
        Boolean result = redisTemplate.execute(setAndExpireScript,
                Collections.singletonList(ONLINE_CLIENT_REDIS_PREFIX + clientId), sessionStr, expire);

        return null != result && result.equals(Boolean.TRUE);
    }

    public SessionDistributed get(final String clientId) {
        ValueOperations<String, String> value = redisTemplate.opsForValue();
        String sessionStr = value.get(ONLINE_CLIENT_REDIS_PREFIX + clientId);
        if (null != sessionStr) {
            log.debug(sessionStr);
            return gson.fromJson(sessionStr, SessionDistributed.class);
        }
        return null;
    }

    public SessionDistributed get(final String clientId, int expire) {
        String expireStr = String.valueOf(Math.round(expire*2.0f));
        String sessionStr = redisTemplate.execute(getAndExpireScript,
                Collections.singletonList(ONLINE_CLIENT_REDIS_PREFIX + clientId), expireStr);
        if (null != sessionStr) {
            log.debug(sessionStr);
            return gson.fromJson(sessionStr, SessionDistributed.class);
        }
        return null;
    }

    public SessionDistributed remove(final String clientId) {
        String sessionStr = redisTemplate.execute(getAndRemoveScript,
                Collections.singletonList(ONLINE_CLIENT_REDIS_PREFIX + clientId));
        if (null != sessionStr) {
            return gson.fromJson(sessionStr, SessionDistributed.class);
        }
        return null;
    }

    public boolean remove(final String clientId, final String sessionId) {
        Boolean result = redisTemplate.execute(removeIfScript,
                Collections.singletonList(ONLINE_CLIENT_REDIS_PREFIX + clientId),
                "sessionId", sessionId);
        return null != result && result.equals(Boolean.TRUE);
    }
}