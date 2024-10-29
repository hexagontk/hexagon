package com.hexagontk.http.server.netty

import com.hexagontk.core.Platform
import com.hexagontk.core.fieldsMapOf
import com.hexagontk.core.security.createKeyManagerFactory
import com.hexagontk.core.security.createTrustManagerFactory
import com.hexagontk.http.HttpFeature
import com.hexagontk.http.HttpFeature.*
import com.hexagontk.http.SslSettings
import com.hexagontk.http.model.HttpProtocol
import com.hexagontk.http.model.HttpProtocol.*
import com.hexagontk.http.server.HttpServer
import com.hexagontk.http.server.HttpServerPort
import com.hexagontk.http.server.HttpServerSettings
import com.hexagontk.http.handlers.HttpHandler
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.ssl.ClientAuth.OPTIONAL
import io.netty.handler.ssl.ClientAuth.REQUIRE
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import io.netty.util.concurrent.DefaultEventExecutorGroup
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit.SECONDS
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.TrustManagerFactory

/**
 * Implements [HttpServerPort] using Netty [Channel].
 */
open class NettyHttpServer(
    private val bossGroupThreads: Int = 1,
    private val workerGroupThreads: Int = 0,
    private val executorThreads: Int = Platform.cpuCount * 2,
    private val soBacklog: Int = 4 * 1_024,
    private val soReuseAddr: Boolean = true,
    private val soKeepAlive: Boolean = true,
    private val shutdownQuietSeconds: Long = 0,
    private val shutdownTimeoutSeconds: Long = 0,
    private val keepAliveHandler: Boolean = true,
    private val httpAggregatorHandler: Boolean = true,
    private val chunkedHandler: Boolean = true,
    private val enableWebsockets: Boolean = true,
) : HttpServerPort {

    private var nettyChannel: Channel? = null
    private var bossEventLoop: MultithreadEventLoopGroup? = null
    private var workerEventLoop: MultithreadEventLoopGroup? = null

    constructor() : this(
        bossGroupThreads = 1,
        workerGroupThreads = 0,
        executorThreads = Platform.cpuCount * 2,
        soBacklog = 4 * 1_024,
        soReuseAddr = true,
        soKeepAlive = true,
        shutdownQuietSeconds = 0,
        shutdownTimeoutSeconds = 0,
        keepAliveHandler = true,
        httpAggregatorHandler = true,
        chunkedHandler = true,
        enableWebsockets = true,
    )

    override fun runtimePort(): Int =
        (nettyChannel?.localAddress() as? InetSocketAddress)?.port
            ?: error("Error fetching runtime port")

    override fun started() =
        nettyChannel?.isOpen ?: false

    override fun startUp(server: HttpServer) {
        val bossGroup = groupSupplier(bossGroupThreads)
        val workerGroup =
            if (workerGroupThreads < 0) bossGroup
            else groupSupplier(workerGroupThreads)
        val executorGroup =
            if (executorThreads > 0) DefaultEventExecutorGroup(executorThreads)
            else null

        try {
            val settings = server.settings
            val sslSettings = settings.sslSettings
            val handlers: Map<HttpMethod, HttpHandler> =
                server.handler.addPrefix(settings.contextPath)
                    .byMethod()
                    .mapKeys { HttpMethod.valueOf(it.key.toString()) }

            val nettyServer = serverBootstrapSupplier(bossGroup, workerGroup)
                .childHandler(createInitializer(sslSettings, handlers, executorGroup, settings))

            val address = settings.bindAddress
            val port = settings.bindPort
            val future = nettyServer.bind(address, port).sync()

            nettyChannel = future.channel()
            bossEventLoop = bossGroup
            workerEventLoop = workerGroup
        }
        catch (_: Exception) {
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
            executorGroup?.shutdownGracefully()
        }
    }

    open fun groupSupplier(it: Int): MultithreadEventLoopGroup =
        NioEventLoopGroup(it)

    open fun serverBootstrapSupplier(
        bossGroup: MultithreadEventLoopGroup,
        workerGroup: MultithreadEventLoopGroup,
    ): ServerBootstrap =
        ServerBootstrap().group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel::class.java)
            .option(ChannelOption.SO_BACKLOG, soBacklog)
            .option(ChannelOption.SO_REUSEADDR, soReuseAddr)
            .childOption(ChannelOption.SO_KEEPALIVE, soKeepAlive)
            .childOption(ChannelOption.SO_REUSEADDR, soReuseAddr)

    private fun createInitializer(
        sslSettings: SslSettings?,
        handlers: Map<HttpMethod, HttpHandler>,
        group: DefaultEventExecutorGroup?,
        settings: HttpServerSettings
    ) =
        when {
            sslSettings != null -> sslInitializer(sslSettings, handlers, group, settings)
            else -> HttpChannelInitializer(
                handlers,
                group,
                settings,
                keepAliveHandler,
                httpAggregatorHandler,
                chunkedHandler,
                enableWebsockets,
            )
        }

    private fun sslInitializer(
        sslSettings: SslSettings,
        handlers: Map<HttpMethod, HttpHandler>,
        group: DefaultEventExecutorGroup?,
        settings: HttpServerSettings
    ): HttpsChannelInitializer =
        HttpsChannelInitializer(
            handlers,
            sslContext(sslSettings),
            sslSettings,
            group,
            settings,
            keepAliveHandler,
            httpAggregatorHandler,
            chunkedHandler,
            enableWebsockets,
        )

    private fun sslContext(sslSettings: SslSettings): SslContext {
        val keyManager = keyManagerFactory(sslSettings)

        val sslContextBuilder = SslContextBuilder
            .forServer(keyManager)
            .clientAuth(if (sslSettings.clientAuth) REQUIRE else OPTIONAL)

        val trustManager = trustManagerFactory(sslSettings)

        return if (trustManager == null) sslContextBuilder.build()
            else sslContextBuilder.trustManager(trustManager).build()
    }

    private fun trustManagerFactory(sslSettings: SslSettings): TrustManagerFactory? {
        val trustStoreUrl = sslSettings.trustStore ?: return null
        return createTrustManagerFactory(trustStoreUrl, sslSettings.trustStorePassword)
    }

    private fun keyManagerFactory(sslSettings: SslSettings): KeyManagerFactory {
        val keyStoreUrl = sslSettings.keyStore ?: error("")
        return createKeyManagerFactory(keyStoreUrl, sslSettings.keyStorePassword)
    }

    override fun shutDown() {
        workerEventLoop
            ?.shutdownGracefully(shutdownQuietSeconds, shutdownTimeoutSeconds, SECONDS)?.sync()
        bossEventLoop
            ?.shutdownGracefully(shutdownQuietSeconds, shutdownTimeoutSeconds, SECONDS)?.sync()

        nettyChannel = null
        bossEventLoop = null
        workerEventLoop = null
    }

    override fun supportedProtocols(): Set<HttpProtocol> =
        setOf(HTTP, HTTPS, HTTP2)

    override fun supportedFeatures(): Set<HttpFeature> =
        setOf(ZIP, COOKIES, MULTIPART, WEBSOCKETS, SSE)

    override fun options(): Map<String, *> =
        fieldsMapOf(
            NettyHttpServer::bossGroupThreads to bossGroupThreads,
            NettyHttpServer::workerGroupThreads to workerGroupThreads,
            NettyHttpServer::executorThreads to executorThreads,
            NettyHttpServer::soBacklog to soBacklog,
            NettyHttpServer::soKeepAlive to soKeepAlive,
            NettyHttpServer::shutdownQuietSeconds to shutdownQuietSeconds,
            NettyHttpServer::shutdownTimeoutSeconds to shutdownTimeoutSeconds,
            NettyHttpServer::keepAliveHandler to keepAliveHandler,
            NettyHttpServer::httpAggregatorHandler to httpAggregatorHandler,
            NettyHttpServer::chunkedHandler to chunkedHandler,
            NettyHttpServer::enableWebsockets to enableWebsockets,
        )
}
