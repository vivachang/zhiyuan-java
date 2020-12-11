package com.forest.dataacquisitionserver.protocol;

import com.forest.dataacquisitionserver.netty.NettyAttributeKey;
import com.forest.dataacquisitionserver.session.Session;
import com.forest.dataacquisitionserver.session.SessionDistributed;
import com.forest.dataacquisitionserver.session.SessionDistributedManager;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.util.Attribute;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageHandlerMapper {
    private ConcurrentMap<String, MethodInvoker> invokerMap = new ConcurrentHashMap<>();
    private final Gson gson;
    private final ProtocolUtil protocolUtil;
    private final SessionDistributedManager sessionDistributedManager;

    public MessageHandlerMapper putInvoker(final String method, final MethodInvoker invoker) {
        invokerMap.put(method, invoker);
        return this;
    }

    /**
     * 判断数据是否为空和协议版本
     * @param ctx
     * @param jsonRequestString
     * @throws Exception
     */
    public void callMethod(ChannelHandlerContext ctx, final String jsonRequestString) throws Exception {
        // 数据对象化
        RequestPacket packet = gson.fromJson(jsonRequestString, RequestPacket.class);
        // 数据不能为空
        if (null == packet) {
            protocolUtil.onProtocolError(ctx.channel(), ProtocolCode.INVALID_REQUEST);
        }
        assert packet != null;
        // 判断数据版本是否符合要求
        if (null == packet.getType() ||
                null == packet.getVersion() || !packet.getVersion().equals(ProtocolVersion.V1_0)) {
            protocolUtil.onProtocolError(ctx.channel(), packet.getId(), ProtocolCode.INVALID_REQUEST);
        }
        PacketType type = packet.getType();
        // 继续处理消息
        callMethod(ctx, packet.getId(), type.getTypeString(), packet.getParams());
    }

    /**
     * 处理消息
     * @param ctx
     * @param id
     * @param method
     * @param params
     * @throws Exception
     */
    public void callMethod(ChannelHandlerContext ctx, final JsonElement id,
                           final String method, final JsonElement params) throws Exception {

        Attribute<Session> sessionAttr = ctx.channel().attr(NettyAttributeKey.SESSION);
        Session session = sessionAttr.get();
        // 当session为空且不是认证请求时返回错误
        if (null == session && !method.equals(PacketType.AUTH.getTypeString())) {
            protocolUtil.onProtocolError(ctx.channel(), id, ProtocolCode.UNAUTHORIZED);
        }
        // 非认证session判断
        if (null != session) {
            SessionDistributed sessionDistributed = sessionDistributedManager.get(session.getClientId(),
                    session.getExpire());
            if (null == sessionDistributed) {
                Attribute<Boolean> closedAttr = ctx.channel().attr(NettyAttributeKey.CLOSED);
                closedAttr.set(true);
                ctx.channel().close();
                log.warn("Session distributed not exist");
                return;
            } else if (!ctx.channel().id().asLongText().equals(sessionDistributed.getSessionId())) {
                Attribute<Boolean> closedAttr = ctx.channel().attr(NettyAttributeKey.CLOSED);
                closedAttr.set(true);
                ctx.channel().close();
                log.warn("Session distributed not equals");
                return;
            } else {
                if (method.equals(PacketType.AUTH.getTypeString())) {
                    log.warn("Client {} already authorized", session.getClientId());
                    return;
                }
            }
        }
        // 通过上报数据中的方法确定数据处理类
        MethodInvoker invoker = invokerMap.get(method);
        if (null != invoker) {
            invoker.call(ctx, id, params);
        } else {
            protocolUtil.onProtocolError(ctx.channel(), id, ProtocolCode.INVALID_REQUEST);
        }
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ResponsePacket responsePacket;
        if (cause instanceof ProtocolException) {
            ProtocolException protocolException = (ProtocolException) cause;
            responsePacket = protocolUtil.buildErrorResponse(protocolException.getId(),
                    protocolException.getProtocolCode());
        } else {
            Attribute<Boolean> closedAttr = ctx.channel().attr(NettyAttributeKey.CLOSED);
            closedAttr.set(true);

            if (cause instanceof IOException) {
                ctx.channel().close();
                return;
            }

            if (cause instanceof TooLongFrameException) {
                responsePacket = protocolUtil.buildErrorResponse(ProtocolCode.TOO_LONG_FRAME);
            } else if (cause instanceof CorruptedFrameException) {
                responsePacket = protocolUtil.buildErrorResponse(ProtocolCode.NOT_WELL_FORMED);
            } else if (cause instanceof JsonSyntaxException) {
                responsePacket = protocolUtil.buildErrorResponse(ProtocolCode.INVALID_REQUEST);
            } else {
                responsePacket = protocolUtil.buildErrorResponse(ProtocolCode.INTERNAL_ERROR);
            }
        }

        String response = gson.toJson(responsePacket, ResponsePacket.class);

        log.debug(response);

        ChannelFuture f = ctx.writeAndFlush(response);
        f.addListener(ChannelFutureListener.CLOSE);
    }
}
