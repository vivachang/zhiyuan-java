package com.forest.dataacquisitionserver.netty;

import com.forest.dataacquisitionserver.protocol.MessageHandlerMapper;
import com.forest.dataacquisitionserver.session.Session;
import com.forest.dataacquisitionserver.session.SessionDistributedManager;
import com.forest.dataacquisitionserver.session.SessionManager;
import com.forest.dataacquisitionserver.util.MessageUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import io.netty.util.ReferenceCountUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * netty消息接收处理
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ChannelHandler.Sharable
public class NettyServerMessageHandler extends ChannelInboundHandlerAdapter {
    private final MessageHandlerMapper messageHandlerMapper;
    private final SessionManager sessionManager;
    private final SessionDistributedManager sessionDistributedManager;
    private final MessageUtil messageUtil;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        Attribute<Session> sessionAttr = ctx.channel().attr(NettyAttributeKey.SESSION);
        Session session = sessionAttr.get();
        if (null != session) {
            sessionManager.remove(session.getClientId(), session);
            sessionDistributedManager.remove(session.getClientId(), session.getSessionDistributedHash());
            log.debug("Client offline:",session.getClientId());
            messageUtil.send(messageUtil.ofOfflineMessage(session.getClientId()));
        }

        log.debug("Bound Channel Count is " + sessionManager.size());
    }

    /**
     * 此处通过匹配上报数据中方法匹配对应方法
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            String message = ((ByteBuf) msg).toString(StandardCharsets.UTF_8);
            if (log.isInfoEnabled()) {
                String s = message.replaceAll("[\r\n]+", "");
                s = s.replaceAll("\\s+", " ");
                Attribute<Session> sessionAttr = ctx.channel().attr(NettyAttributeKey.SESSION);
                Session session = sessionAttr.get();
                if (null != session) {
                    log.info("Client:{}, id:{}, message:{}",
                            ctx.channel().remoteAddress().toString(), session.getClientId(), s);
                } else {
                    log.info("Client:{}, id:-, message:{}",
                            ctx.channel().remoteAddress().toString(), s);
                }
            }
            Attribute<Boolean> closedAttr = ctx.channel().attr(NettyAttributeKey.CLOSED);
            if (null == closedAttr.get() || closedAttr.get().equals(Boolean.FALSE)) {
                // 调用messageHandlerMapper去匹配方法
                messageHandlerMapper.callMethod(ctx, message);
            } else {
                log.info("Ctx is closed, ignore:{}", message);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);

        if(evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent)evt;
            if (event.state() == IdleState.READER_IDLE) {
                if (ctx.pipeline().names().contains(NettyConstant.authMaxWaitHandlerName)) {
                    log.info("Authentication timeout");
                } else if (ctx.pipeline().names().contains(NettyConstant.heartbeatTimeoutHandlerName)) {
                    log.info("Heartbeat timeout");
                }
                Attribute<Boolean> closedAttr = ctx.channel().attr(NettyAttributeKey.CLOSED);
                closedAttr.set(true);
                ctx.channel().close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);

        log.warn(cause.getMessage());

        messageHandlerMapper.exceptionCaught(ctx, cause);
    }
}