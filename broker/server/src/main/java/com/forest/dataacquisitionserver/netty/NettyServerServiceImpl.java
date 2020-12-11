package com.forest.dataacquisitionserver.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

/**
 * netty启动配置
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NettyServerServiceImpl implements NettyService, DisposableBean {
    private final NettyProperties nettyProperties;
    private final NettyServerChannelInitializer nettyServerChannelInitializer;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    /**
     * netty启动方法
     * @throws InterruptedException
     */
    @Override
    public void start() throws InterruptedException {
        ServerBootstrap b = new ServerBootstrap();
        this.bossGroup = createEventLoopGroup();
        // 判断当前系统，通过设置不同的EventLoopGroup提高性能
        if (bossGroup instanceof EpollEventLoopGroup) {
            groupsEpoll(b, nettyProperties.getWorkerCount());
        } else {
            groupsNio(b, nettyProperties.getWorkerCount());
        }
        try {
            InetSocketAddress address = new InetSocketAddress(
                    nettyProperties.getTcpHost(), nettyProperties.getTcpPort());
            log.info("Netty service started on: " + address.toString());
            ChannelFuture future = b.bind(address).sync();
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    @Override
    public void destroy() throws Exception {
        stop();
    }

    private EventLoopGroup createEventLoopGroup() {
        try {
            return new EpollEventLoopGroup(nettyProperties.getBossCount());
        } catch (final Throwable ex) {
            return new NioEventLoopGroup(nettyProperties.getBossCount());
        }
    }

    /**
     * 设置bootstrap
     * @param bootstrap
     * @param nThreads
     */
    private void groupsEpoll(final ServerBootstrap bootstrap, final int nThreads) {
        workerGroup = new EpollEventLoopGroup(nThreads);
        bootstrap.group(bossGroup, workerGroup)
                .channel(EpollServerSocketChannel.class)
                .option(EpollChannelOption.TCP_CORK, true)
                .option(EpollChannelOption.SO_KEEPALIVE, true)
                .option(EpollChannelOption.SO_BACKLOG, nettyProperties.getBacklog())
                .option(EpollChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(EpollChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(nettyServerChannelInitializer);
    }

    /**
     * 设置bootstrap
     * @param bootstrap
     * @param nThreads
     */
    private void groupsNio(final ServerBootstrap bootstrap, final int nThreads) {
        workerGroup = new NioEventLoopGroup(nThreads);
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(EpollChannelOption.TCP_CORK, true)
                .option(EpollChannelOption.SO_KEEPALIVE, true)
                .option(EpollChannelOption.SO_BACKLOG, nettyProperties.getBacklog())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(nettyServerChannelInitializer);
    }

    /**
     * netty停止方法
     */
    private void stop() {
        try {
            if (null != workerGroup) {
                workerGroup.shutdownGracefully().wait();
            }
            if (null != bossGroup) {
                bossGroup.shutdownGracefully().await();
            }
        } catch (InterruptedException e) {
            throw new NettyServerStartFailedException("Netty container stop interrupted", e);
        }
    }
}
