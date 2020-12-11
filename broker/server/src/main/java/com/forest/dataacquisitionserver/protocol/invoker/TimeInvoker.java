package com.forest.dataacquisitionserver.protocol.invoker;

import com.forest.dataacquisitionserver.protocol.MethodInvoker;
import com.forest.dataacquisitionserver.protocol.ProtocolCode;
import com.forest.dataacquisitionserver.protocol.ProtocolUtil;
import com.forest.dataacquisitionserver.protocol.ResponsePacket;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 时间同步
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TimeInvoker implements MethodInvoker {
    private final Gson gson;
    private final ProtocolUtil protocolUtil;

    @Override
    public void call(ChannelHandlerContext ctx, JsonElement id, JsonElement params) {
        if (null == id) {
            protocolUtil.onProtocolError(ctx.channel(), ProtocolCode.INVALID_REQUEST);
        }
        Long currentTimeStamp = System.currentTimeMillis();
        ResponsePacket response = protocolUtil.buildResponse(id,
                new JsonPrimitive(currentTimeStamp));
        ctx.channel().writeAndFlush(gson.toJson(response, ResponsePacket.class));
    }
}
