package com.forest.dataacquisitionserver.service;

import com.forest.dataacquisitionserver.feign.ClientClient;
import com.forest.dataacquisitionserver.netty.NettyAttributeKey;
import com.forest.dataacquisitionserver.session.Session;
import com.forest.dataacquisitionserver.session.SessionDistributed;
import com.forest.dataacquisitionserver.session.SessionDistributedManager;
import com.forest.dataacquisitionserver.session.SessionManager;
import feign.Feign;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {
    @Value("${iot.client}")
    private String AUTH_CLIENT_REDIS_PREFIX;
    private final RedisTemplate<String, String> redisTemplate;
    private final SessionManager sessionManager;
    private final SessionDistributedManager sessionDistributedManager;
    private final Registration registration;

    /**
     * 判断终端id是否已经注册到系统
     * @param clientId
     * @return
     */
    public boolean auth(final String clientId) {
        HashOperations<String, String, String> hash = redisTemplate.opsForHash();
        return hash.hasKey(AUTH_CLIENT_REDIS_PREFIX, clientId);
    }

    /**
     * 终端登出
     * @param clientId
     * @param sessionId
     */
    public void logout(final String clientId, final String sessionId) {
        Session session = sessionManager.get(clientId);
        if (null != session) {
            Channel channel = session.getChannel();
            if (channel.id().asLongText().equals(sessionId)) {
                Attribute<Boolean> closedAttr = channel.attr(NettyAttributeKey.CLOSED);
                closedAttr.set(true);

                Attribute<Session> sessionAttr = channel.attr(NettyAttributeKey.SESSION);
                sessionAttr.set(null);

                sessionManager.remove(clientId, session);
                channel.close();
            }
        }
    }

    /**
     * 终端登录
     * @param clientId
     * @param session
     * @return
     */
    public boolean login(final String clientId, Session session) {
        if (sessionManager.put(clientId, session)) {
            Channel channel = session.getChannel();
            Attribute<Session> sessionRef = channel.attr(NettyAttributeKey.SESSION);
            sessionRef.set(session);
            return true;
        }
        return false;
    }

    /**
     * 踢掉终端
     * @param currentNodeId
     * @param clientId
     */
    public void kick(final String currentNodeId, final String clientId) {
        SessionDistributed oldSessionDistributed = sessionDistributedManager.remove(clientId);
        if (null != oldSessionDistributed) {
            if (currentNodeId.equals(oldSessionDistributed.getNodeId())) {
                this.logout(clientId, oldSessionDistributed.getSessionId());
            } else {
                kickRemoteClient(oldSessionDistributed.getNodeUri(),
                    clientId, oldSessionDistributed.getSessionId());
            }
        }
    }

    public void kick(final String clientId) {
        kick(registration.getInstanceId(), clientId);
    }

    private void kickRemoteClient(final String uri, final String clientId, final String sessionId) {
        ClientClient clientClient = Feign.builder().target(ClientClient.class, uri);
        String result = clientClient.kickLocal(clientId, sessionId);
        log.debug("Kick out client {} result {}", clientId, result);
    }
}
