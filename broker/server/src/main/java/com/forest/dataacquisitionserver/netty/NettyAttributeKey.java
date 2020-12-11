package com.forest.dataacquisitionserver.netty;

import com.forest.dataacquisitionserver.session.Session;
import io.netty.util.AttributeKey;

public interface NettyAttributeKey {
    AttributeKey<Session> SESSION = AttributeKey.valueOf("session");
    AttributeKey<Boolean> KICK = AttributeKey.valueOf("kick");
    AttributeKey<Boolean> CLOSED = AttributeKey.valueOf("closed");
}
