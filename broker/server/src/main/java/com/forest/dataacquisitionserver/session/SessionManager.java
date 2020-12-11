package com.forest.dataacquisitionserver.session;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class SessionManager {
    private ConcurrentMap<String, Session> sessionCache = new ConcurrentHashMap<>();

    public boolean put(final String clientId, Session session) {
        return null == this.sessionCache.putIfAbsent(clientId, session);
    }

    public Session get(final String clientId) {
        return this.sessionCache.get(clientId);
    }

    public Session remove(final String clientId) {
        return this.sessionCache.remove(clientId);
    }

    public boolean remove(final String clientId, Session session) {
        return sessionCache.remove(clientId, session);
    }

    public long size() {
        return this.sessionCache.size();
    }
}
