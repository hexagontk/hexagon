package com.hexagontk.http.server.netty.epoll

import com.hexagontk.http.server.HttpServerPort
import com.hexagontk.http.server.netty.NettyHttpServer
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelOption
import io.netty.channel.MultiThreadIoEventLoopGroup
import io.netty.channel.MultithreadEventLoopGroup
import io.netty.channel.epoll.EpollChannelOption
import io.netty.channel.epoll.EpollIoHandler
import io.netty.channel.epoll.EpollServerSocketChannel
import java.util.concurrent.Executor
import java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor

/**
 * Implements [HttpServerPort] using Netty Epoll [Channel].
 */
class NettyEpollHttpServer(
    bossGroupThreads: Int = 1,
    workerGroupThreads: Int = 0,
    executor: Executor? = newVirtualThreadPerTaskExecutor(),
    private val soBacklog: Int = 4 * 1_024,
    private val soReuseAddr: Boolean = true,
    private val soKeepAlive: Boolean = true,
    shutdownQuietSeconds: Long = 0,
    shutdownTimeoutSeconds: Long = 0,
    keepAliveHandler: Boolean = true,
    httpAggregatorHandler: Boolean = true,
    chunkedHandler: Boolean = true,
    enableWebsockets: Boolean = true,
) : NettyHttpServer(
    bossGroupThreads,
    workerGroupThreads,
    executor,
    soBacklog,
    soReuseAddr,
    soKeepAlive,
    shutdownQuietSeconds,
    shutdownTimeoutSeconds,
    keepAliveHandler,
    httpAggregatorHandler,
    chunkedHandler,
    enableWebsockets,
) {

    constructor() :
        this(
            bossGroupThreads = 1,
            workerGroupThreads = 0,
            executor = newVirtualThreadPerTaskExecutor(),
            soBacklog = 4 * 1_024,
            soReuseAddr = true,
            soKeepAlive = true,
            shutdownQuietSeconds = 0,
            shutdownTimeoutSeconds = 0,
            keepAliveHandler = true,
            httpAggregatorHandler = true,
            chunkedHandler = true,
            enableWebsockets = true
        )

    override fun groupSupplier(it: Int): MultithreadEventLoopGroup =
        MultiThreadIoEventLoopGroup(EpollIoHandler.newFactory())

    override fun serverBootstrapSupplier(
        bossGroup: MultithreadEventLoopGroup,
        workerGroup: MultithreadEventLoopGroup,
    ): ServerBootstrap =
        ServerBootstrap().group(bossGroup, workerGroup)
            .channel(EpollServerSocketChannel::class.java)
            .option(EpollChannelOption.SO_REUSEPORT, true)
            .option(ChannelOption.SO_BACKLOG, soBacklog)
            .option(ChannelOption.SO_REUSEADDR, soReuseAddr)
            .childOption(ChannelOption.SO_KEEPALIVE, soKeepAlive)
            .childOption(ChannelOption.SO_REUSEADDR, soReuseAddr)
}
