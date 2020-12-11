package com.forest.dataacquisitionserver.protocol.invoker;

import com.forest.dataacquisitionserver.netty.NettyConstant;
import com.forest.dataacquisitionserver.protocol.MethodInvoker;
import com.forest.dataacquisitionserver.protocol.ProtocolCode;
import com.forest.dataacquisitionserver.protocol.ProtocolUtil;
import com.forest.dataacquisitionserver.protocol.ResponsePacket;
import com.forest.dataacquisitionserver.protocol.params.AuthParams;
import com.forest.dataacquisitionserver.service.ClientService;
import com.forest.dataacquisitionserver.session.Session;
import com.forest.dataacquisitionserver.session.SessionDistributed;
import com.forest.dataacquisitionserver.session.SessionDistributedManager;
import com.forest.dataacquisitionserver.session.SessionManager;
import com.forest.dataacquisitionserver.util.MessageUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

/**
 * 认证处理
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInvoker implements MethodInvoker {
    private final Gson gson;
    private final Registration registration;
    private final SessionDistributedManager sessionDistributedManager;
    private final SessionManager sessionManager;
    private final ProtocolUtil protocolUtil;
    private final ClientService clientService;
    private final MessageUtil messageUtil;

    /**
     * 认证实现
     * @param ctx
     * @param id 请求id
     * @param params 请求数据
     * @throws Exception
     */
    @Override
    public void call(ChannelHandlerContext ctx, JsonElement id, JsonElement params) throws Exception {
        // id为空或者数据为空
        if (null == id || null == params) {
            protocolUtil.onProtocolError(ctx.channel(), id, ProtocolCode.INVALID_REQUEST);
        }
        AuthParams authParams = gson.fromJson(params, AuthParams.class);
        if (null == authParams) {
            protocolUtil.onProtocolError(ctx.channel(), id, ProtocolCode.INVALID_REQUEST);
        }
        assert authParams != null;
        String clientId = authParams.getClientId();
        int expire = authParams.getKeepAliveInterval(); // 获取硬件端自定义心跳间隔
        if (null == authParams.getClientId() || expire <= 0) {
            protocolUtil.onProtocolError(ctx.channel(), id, ProtocolCode.INVALID_PARAMS);
        }
        // 判断终端是否已经注册到系统
        if (!clientService.auth(clientId)) {
            protocolUtil.onProtocolError(ctx.channel(), id, ProtocolCode.CLIENT_NOT_FOUND);
        }

        SessionDistributed sessionDistributed = new SessionDistributed();
        sessionDistributed.setClientId(clientId);
        InetSocketAddress remoteAddress = (InetSocketAddress)ctx.channel().remoteAddress();
        sessionDistributed.setRemoteHost(remoteAddress.getHostString());
        sessionDistributed.setRemotePort(remoteAddress.getPort());
        sessionDistributed.setExpire(expire);
        sessionDistributed.setNodeId(registration.getInstanceId());
        sessionDistributed.setNodeUri(registration.getUri().toString());
        sessionDistributed.setSessionId(ctx.channel().id().asLongText());

        Session session = new Session();
        session.setClientId(clientId);
        session.setChannel(ctx.channel());
        session.setSessionDistributedHash(sessionDistributed.getSessionId());
        session.setExpire(expire);
        // 先踢掉终端
        clientService.kick(sessionDistributed.getNodeId(), clientId);
        // 在注册
        if (clientService.login(clientId, session)) {
            log.debug("Client {} authentication passed", clientId);
            if (sessionDistributedManager.put(clientId, sessionDistributed)) {
                log.debug("Client {} authentication distributed passed", clientId);

                if (ctx.channel().pipeline().names().contains(NettyConstant.authMaxWaitHandlerName)) {
                    ctx.channel().pipeline().remove(NettyConstant.authMaxWaitHandlerName);
                }
                log.debug("Client online 1");
                expire = Math.round(expire * 1.5f);
                ctx.channel().pipeline().addFirst(NettyConstant.heartbeatTimeoutHandlerName, new IdleStateHandler(
                        expire, 0, 0));
                log.debug("Client online 2");
                ResponsePacket responsePacket = protocolUtil.buildResponse(id, new JsonPrimitive("ok"));
                ctx.channel().writeAndFlush(gson.toJson(responsePacket, ResponsePacket.class));
                log.debug("Client online 3");
                messageUtil.send(messageUtil.ofOnlineMessage(session.getClientId()));
            } else {
                log.warn("Client {} authentication distributed failed", clientId);
                clientService.logout(clientId, sessionDistributed.getSessionId());
                protocolUtil.onProtocolError(ctx.channel(), ProtocolCode.LOGON_OCCUPIED);
            }
        } else {
            log.warn("Client {} authentication failed", clientId);
            protocolUtil.onProtocolError(ctx.channel(), ProtocolCode.LOGON_OCCUPIED);
        }

        log.debug("Bound Channel Count is " + sessionManager.size());
    }
}
