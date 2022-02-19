package com.hexagonkt.http.server.netty

import com.hexagonkt.core.Jvm
import com.hexagonkt.core.fieldsMapOf
import com.hexagonkt.core.security.loadKeyStore
import com.hexagonkt.http.SslSettings
import com.hexagonkt.http.model.HttpProtocol
import com.hexagonkt.http.model.HttpProtocol.*
import com.hexagonkt.http.server.HttpServer
import com.hexagonkt.http.server.HttpServerFeature
import com.hexagonkt.http.server.HttpServerFeature.ASYNC
import com.hexagonkt.http.server.HttpServerFeature.ZIP
import com.hexagonkt.http.server.HttpServerPort
import com.hexagonkt.http.server.HttpServerSettings
import com.hexagonkt.http.server.handlers.PathHandler
import com.hexagonkt.http.server.handlers.path
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.ssl.ClientAuth.OPTIONAL
import io.netty.handler.ssl.ClientAuth.REQUIRE
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.stream.ChunkedWriteHandler
import io.netty.util.concurrent.DefaultEventExecutorGroup
import io.netty.util.concurrent.EventExecutorGroup
import java.net.InetSocketAddress
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.TrustManagerFactory
import kotlin.Int.Companion.MAX_VALUE

/**
 * Implements [HttpServerPort] using Netty [Channel].
 *
 * TODO Add HTTP/2 support: https://www.baeldung.com/netty-http2
 * TODO Add SSE support: https://github.com/marcusyates/netty-sse-example
 */
class NettyServerAdapter(
    private val bossGroupThreads: Int = 1,
    private val workerGroupThreads: Int = 0,
    private val executorThreads: Int = Jvm.cpuCount * 2,
    private val soBacklog: Int = 4 * 1_024,
    private val soReuseAddr: Boolean = true,
    private val soKeepAlive: Boolean = true,
) : HttpServerPort {

    private var nettyChannel: Channel? = null
    private var bossEventLoop: NioEventLoopGroup? = null
    private var workerEventLoop: NioEventLoopGroup? = null

    constructor() : this(
        bossGroupThreads = 1,
        workerGroupThreads = 0,
        executorThreads = Jvm.cpuCount * 2,
        soBacklog = 4 * 1_024,
        soReuseAddr = true,
        soKeepAlive = true,
    )

    override fun runtimePort(): Int =
        (nettyChannel?.localAddress() as? InetSocketAddress)?.port
            ?: error("Error fetching runtime port")

    override fun started() =
        nettyChannel?.isOpen ?: false

    override fun startUp(server: HttpServer) {
        val bossGroup = NioEventLoopGroup(bossGroupThreads)
        val workerGroup = NioEventLoopGroup(workerGroupThreads)
        val executorGroup =
            if (executorThreads > 0) DefaultEventExecutorGroup(executorThreads)
            else null

        try {
            val nettyServer = ServerBootstrap()
            val settings = server.settings
            val sslSettings = settings.sslSettings
            val handlers: Map<HttpMethod, PathHandler> =
                path(settings.contextPath, server.handlers)
                    .byMethod()
                    .mapKeys { HttpMethod.valueOf(it.key.toString()) }

            nettyServer.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
//                .option(EpollChannelOption.SO_REUSEPORT, true)
                .option(ChannelOption.SO_BACKLOG, soBacklog)
                .option(ChannelOption.SO_REUSEADDR, soReuseAddr)
                .childOption(ChannelOption.SO_KEEPALIVE, soKeepAlive)
                .childOption(ChannelOption.SO_REUSEADDR, soReuseAddr)
                .childHandler(createInitializer(sslSettings, handlers, executorGroup, settings))

            val address = settings.bindAddress
            val port = settings.bindPort
            val future = nettyServer.bind(address, port).sync()

            nettyChannel = future.channel()
            bossEventLoop = bossGroup
            workerEventLoop = workerGroup
        }
        catch (e: Exception) {
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
            executorGroup?.shutdownGracefully()
        }
    }

    private fun createInitializer(
        sslSettings: SslSettings?,
        handlers: Map<HttpMethod, PathHandler>,
        group: DefaultEventExecutorGroup?,
        settings: HttpServerSettings
    ) =
        if (sslSettings == null) {
            HttpChannelInitializer(handlers, group, settings)
        } else {
            val keyManager = createKeyManagerFactory(sslSettings)

            val sslContextBuilder = SslContextBuilder
                .forServer(keyManager)
                .clientAuth(if (sslSettings.clientAuth) REQUIRE else OPTIONAL)

            val trustManager = createTrustManagerFactory(sslSettings)

            val sslContext: SslContext =
                if (trustManager == null) sslContextBuilder.build()
                else sslContextBuilder.trustManager(trustManager).build()

            HttpsChannelInitializer(handlers, sslContext, sslSettings, group, settings)
        }

    private fun createTrustManagerFactory(sslSettings: SslSettings): TrustManagerFactory? {
        val trustStoreUrl = sslSettings.trustStore ?: return null

        val trustStorePassword = sslSettings.trustStorePassword
        val trustStore = loadKeyStore(trustStoreUrl, trustStorePassword)
        val trustAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
        val trustManager = TrustManagerFactory.getInstance(trustAlgorithm)

        trustManager.init(trustStore)
        return trustManager
    }

    private fun createKeyManagerFactory(sslSettings: SslSettings): KeyManagerFactory {
        val keyStoreUrl = sslSettings.keyStore ?: error("")
        val keyStorePassword = sslSettings.keyStorePassword
        val keyStore = loadKeyStore(keyStoreUrl, keyStorePassword)
        val keyManager = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        keyManager.init(keyStore, keyStorePassword.toCharArray())
        return keyManager
    }

    override fun shutDown() {
        workerEventLoop?.shutdownGracefully()?.sync()
        bossEventLoop?.shutdownGracefully()?.sync()

        nettyChannel = null
        bossEventLoop = null
        workerEventLoop = null
    }

    override fun supportedProtocols(): Set<HttpProtocol> =
        setOf(HTTP, HTTPS, HTTP2)

    override fun supportedFeatures(): Set<HttpServerFeature> =
        setOf(ZIP, ASYNC)

    override fun options(): Map<String, *> =
        fieldsMapOf(
            NettyServerAdapter::bossGroupThreads to bossGroupThreads,
            NettyServerAdapter::workerGroupThreads to workerGroupThreads,
            NettyServerAdapter::executorThreads to executorThreads,
            NettyServerAdapter::soBacklog to soBacklog,
            NettyServerAdapter::soKeepAlive to soKeepAlive,
        )

    class HttpChannelInitializer(
        private val handlers: Map<HttpMethod, PathHandler>,
        private val executorGroup: EventExecutorGroup?,
        private val settings: HttpServerSettings,
    ) : ChannelInitializer<SocketChannel>() {

        override fun initChannel(channel: SocketChannel) {
            val pipeline = channel.pipeline()

            pipeline.addLast(HttpServerCodec())
            pipeline.addLast(HttpServerKeepAliveHandler())
            pipeline.addLast(HttpObjectAggregator(MAX_VALUE))
            pipeline.addLast(ChunkedWriteHandler())

            if (settings.features.contains(ZIP))
                pipeline.addLast(HttpContentCompressor())

            if (executorGroup == null)
                pipeline.addLast(NettyServerHandler(handlers, null))
            else
                pipeline.addLast(executorGroup, NettyServerHandler(handlers, null))
        }
    }

    class HttpsChannelInitializer(
        private val handlers: Map<HttpMethod, PathHandler>,
        private val sslContext: SslContext,
        private val sslSettings: SslSettings,
        private val executorGroup: EventExecutorGroup?,
        private val settings: HttpServerSettings,
    ) : ChannelInitializer<SocketChannel>() {

        override fun initChannel(channel: SocketChannel) {
            val pipeline = channel.pipeline()
            val sslHandler = sslContext.newHandler(channel.alloc())
            val handlerSsl = if (sslSettings.clientAuth) sslHandler else null

            pipeline.addLast(sslHandler)
            pipeline.addLast(HttpServerCodec())
            pipeline.addLast(HttpServerKeepAliveHandler())
            pipeline.addLast(HttpObjectAggregator(MAX_VALUE))
            pipeline.addLast(ChunkedWriteHandler())

            if (settings.features.contains(ZIP))
                pipeline.addLast(HttpContentCompressor())

            if (executorGroup == null)
                pipeline.addLast(NettyServerHandler(handlers, handlerSsl))
            else
                pipeline.addLast(executorGroup, NettyServerHandler(handlers, handlerSsl))
        }
    }
}
