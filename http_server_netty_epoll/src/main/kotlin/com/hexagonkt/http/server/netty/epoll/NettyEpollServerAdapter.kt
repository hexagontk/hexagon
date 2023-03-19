package com.hexagonkt.http.server.netty.epoll

import com.hexagonkt.core.Jvm
import com.hexagonkt.http.server.HttpServerPort
import com.hexagonkt.http.server.netty.NettyServerAdapter
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
class NettyEpollServerAdapter(
    bossGroupThreads: Int = 1,
    executorThreads: Int = Jvm.cpuCount * 2,
    private val soBacklog: Int = 4 * 1_024,
    private val soReuseAddr: Boolean = true,
    private val soKeepAlive: Boolean = true,
) : NettyServerAdapter(
    bossGroupThreads,
    executorThreads,
    soBacklog,
    soReuseAddr,
    soKeepAlive,
) {

    constructor() : this(1, Jvm.cpuCount * 2, 4 * 1_024, true, true)

    override fun groupSupplier(it: Int): MultithreadEventLoopGroup =
        EpollEventLoopGroup(it)

    override fun serverBootstrapSupplier(bossGroup: MultithreadEventLoopGroup): ServerBootstrap =
        ServerBootstrap().group(bossGroup)
            .channel(EpollServerSocketChannel::class.java)
            .option(EpollChannelOption.SO_REUSEPORT, true)
            .option(ChannelOption.SO_BACKLOG, soBacklog)
            .option(ChannelOption.SO_REUSEADDR, soReuseAddr)
            .childOption(ChannelOption.SO_KEEPALIVE, soKeepAlive)
            .childOption(ChannelOption.SO_REUSEADDR, soReuseAddr)
}
