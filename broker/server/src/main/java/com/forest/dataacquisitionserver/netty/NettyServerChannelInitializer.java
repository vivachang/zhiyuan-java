package com.forest.dataacquisitionserver.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.CharsetUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class NettyServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final NettyServerMessageHandler messageHandler;
    private final NettyProperties nettyProperties;

    /**
     * 添加encode和decode和消息处理类
     * @param ch
     * @throws Exception
     */
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(NettyConstant.authMaxWaitHandlerName, new IdleStateHandler(
                nettyProperties.getAuthMaxWait().getSeconds(),
                0, 0, TimeUnit.SECONDS));
        pipeline.addLast("stringEncoder", new StringEncoder(CharsetUtil.UTF_8));
        pipeline.addLast("jsonDecoder", new JsonObjectDecoder(1024*100));
        pipeline.addLast("message", messageHandler);
    }
}