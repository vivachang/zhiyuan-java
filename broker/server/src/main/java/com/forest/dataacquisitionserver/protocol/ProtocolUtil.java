package com.forest.dataacquisitionserver.protocol;

import com.forest.dataacquisitionserver.netty.NettyAttributeKey;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 返回消息组装类
 */
@Component
@RequiredArgsConstructor
public final class ProtocolUtil {
    private final Gson gson;

    public ResponsePacket buildErrorResponse(JsonElement id, ProtocolCode code) {
        ResponsePacket responsePacket = new ResponsePacket();
        responsePacket.setVersion(ProtocolVersion.V1_0);
        responsePacket.setId(id);
        responsePacket.setError(gson.toJsonTree(code));

        return responsePacket;
    }

    public ResponsePacket buildErrorResponse(ProtocolCode code) {
        ResponsePacket responsePacket = new ResponsePacket();
        responsePacket.setVersion(ProtocolVersion.V1_0);
        responsePacket.setError(gson.toJsonTree(code));

        return responsePacket;
    }

    public ResponsePacket buildResponse(JsonElement id, JsonElement result) {
        ResponsePacket responsePacket = new ResponsePacket();
        responsePacket.setVersion(ProtocolVersion.V1_0);
        responsePacket.setId(id);
        responsePacket.setResult(result);

        return responsePacket;
    }

    public void onProtocolError(Channel channel, ProtocolCode code) {
        Attribute<Boolean> closedAttr = channel.attr(NettyAttributeKey.CLOSED);
        closedAttr.set(true);

        throw new ProtocolException(code);
    }

    public void onProtocolError(Channel channel, JsonElement id, ProtocolCode code) {
        Attribute<Boolean> closedAttr = channel.attr(NettyAttributeKey.CLOSED);
        closedAttr.set(true);

        throw new ProtocolException(id, code);
    }
}
