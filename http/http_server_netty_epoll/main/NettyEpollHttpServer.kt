package com.hexagontk.http.server.netty.epoll

import com.hexagontk.core.Platform
import com.hexagontk.http.server.HttpServerPort
import com.hexagontk.http.server.netty.NettyHttpServer
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelOption
import io.netty.channel.MultithreadEventLoopGroup
import io.netty.channel.epoll.EpollChannelOption
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollServerSocketChannel

/**
 * Implements [HttpServerPort] using Netty Epoll [Channel].
 */
class NettyEpollHttpServer(
    bossGroupThreads: Int = 1,
    workerGroupThreads: Int = 0,
    executorThreads: Int = Platform.cpuCount * 2,
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
    executorThreads,
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
        this(1, 0, Platform.cpuCount * 2, 4 * 1_024, true, true, 0, 0, true, true, true, true)

    override fun groupSupplier(it: Int): MultithreadEventLoopGroup =
        EpollEventLoopGroup(it)

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
