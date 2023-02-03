package com.hexagonkt.http.server.netty

import com.hexagonkt.core.Jvm
import com.hexagonkt.core.fieldsMapOf
import com.hexagonkt.core.security.loadKeyStore
import com.hexagonkt.http.SslSettings
import com.hexagonkt.http.model.HttpProtocol
import com.hexagonkt.http.model.HttpProtocol.*
import com.hexagonkt.http.server.HttpServer
import com.hexagonkt.http.server.HttpServerFeature
import com.hexagonkt.http.server.HttpServerFeature.WEB_SOCKETS
import com.hexagonkt.http.server.HttpServerFeature.ZIP
import com.hexagonkt.http.server.HttpServerPort
import com.hexagonkt.http.server.HttpServerSettings
import com.hexagonkt.http.server.handlers.PathHandler
import com.hexagonkt.http.server.handlers.path
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.HttpServerUpgradeHandler.UpgradeCodecFactory
import io.netty.handler.codec.http2.*
import io.netty.handler.ssl.ClientAuth.OPTIONAL
import io.netty.handler.ssl.ClientAuth.REQUIRE
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.stream.ChunkedWriteHandler
import io.netty.util.AsciiString
import io.netty.util.concurrent.DefaultEventExecutorGroup
import io.netty.util.concurrent.EventExecutorGroup
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit.SECONDS
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.TrustManagerFactory
import kotlin.Int.Companion.MAX_VALUE

/**
 * Implements [HttpServerPort] using Netty [Channel].
 *
 * TODO Add HTTP/2 support: https://www.baeldung.com/netty-http2
 * TODO Add SSE support: https://github.com/marcusyates/netty-sse-example
 */
open class NettyServerAdapter(
    private val bossGroupThreads: Int = 1,
    private val workerGroupThreads: Int = 0,
    private val executorThreads: Int = Jvm.cpuCount * 2,
    private val soBacklog: Int = 4 * 1_024,
    private val soReuseAddr: Boolean = true,
    private val soKeepAlive: Boolean = true,
    private val shutdownQuietSeconds: Long = 0,
    private val shutdownTimeoutSeconds: Long = 0,
) : HttpServerPort {

    private var nettyChannel: Channel? = null
    private var bossEventLoop: MultithreadEventLoopGroup? = null
    private var workerEventLoop: MultithreadEventLoopGroup? = null

    constructor() : this(
        bossGroupThreads = 1,
        workerGroupThreads = 0,
        executorThreads = Jvm.cpuCount * 2,
        soBacklog = 4 * 1_024,
        soReuseAddr = true,
        soKeepAlive = true,
        shutdownQuietSeconds = 0,
        shutdownTimeoutSeconds = 0,
    )

    override fun runtimePort(): Int =
        (nettyChannel?.localAddress() as? InetSocketAddress)?.port
            ?: error("Error fetching runtime port")

    override fun started() =
        nettyChannel?.isOpen ?: false

    override fun startUp(server: HttpServer) {
        val bossGroup = groupSupplier(bossGroupThreads)
        val workerGroup = groupSupplier(workerGroupThreads)
        val executorGroup =
            if (executorThreads > 0) DefaultEventExecutorGroup(executorThreads)
            else null

        try {
            val settings = server.settings
            val sslSettings = settings.sslSettings
            val handlers: Map<HttpMethod, PathHandler> =
                path(settings.contextPath, server.handlers)
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
        catch (e: Exception) {
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
        handlers: Map<HttpMethod, PathHandler>,
        group: DefaultEventExecutorGroup?,
        settings: HttpServerSettings
    ) =
        if (sslSettings != null) {
            val keyManager = createKeyManagerFactory(sslSettings)

            val sslContextBuilder = SslContextBuilder
                .forServer(keyManager)
                .clientAuth(if (sslSettings.clientAuth) REQUIRE else OPTIONAL)

            val trustManager = createTrustManagerFactory(sslSettings)

            val sslContext: SslContext =
                if (trustManager == null) sslContextBuilder.build()
                else sslContextBuilder.trustManager(trustManager).build()

            HttpsChannelInitializer(handlers, sslContext, sslSettings, group, settings)
        } else if (settings.protocol.equals(H2C)) {
            H2CChannelInitializer()
        } else {
            HttpChannelInitializer(handlers, group, settings)
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
        workerEventLoop
            ?.shutdownGracefully(shutdownQuietSeconds, shutdownTimeoutSeconds, SECONDS)?.sync()
        bossEventLoop
            ?.shutdownGracefully(shutdownQuietSeconds, shutdownTimeoutSeconds, SECONDS)?.sync()

        nettyChannel = null
        bossEventLoop = null
        workerEventLoop = null
    }

    override fun supportedProtocols(): Set<HttpProtocol> =
        setOf(HTTP, HTTPS, HTTP2, H2C)

    override fun supportedFeatures(): Set<HttpServerFeature> =
        setOf(ZIP, WEB_SOCKETS)

    override fun options(): Map<String, *> =
        fieldsMapOf(
            NettyServerAdapter::bossGroupThreads to bossGroupThreads,
            NettyServerAdapter::workerGroupThreads to workerGroupThreads,
            NettyServerAdapter::executorThreads to executorThreads,
            NettyServerAdapter::soBacklog to soBacklog,
            NettyServerAdapter::soKeepAlive to soKeepAlive,
            NettyServerAdapter::shutdownQuietSeconds to shutdownQuietSeconds,
            NettyServerAdapter::shutdownTimeoutSeconds to shutdownTimeoutSeconds,
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

            if (settings.zip)
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

            if (settings.zip)
                pipeline.addLast(HttpContentCompressor())

            if (executorGroup == null)
                pipeline.addLast(NettyServerHandler(handlers, handlerSsl))
            else
                pipeline.addLast(executorGroup, NettyServerHandler(handlers, handlerSsl))
        }
    }

    class H2CChannelInitializer : ChannelInitializer<SocketChannel>() {
        class HttpHandler : SimpleChannelInboundHandler<HttpMessage>() {
            override fun channelRead0(ctx: ChannelHandlerContext?, msg: HttpMessage) {
                System.err.println(
                    "Directly talking: " + msg.protocolVersion()
                        .toString() + " (no upgrade was attempted)"
                )
            }
        }

        override fun initChannel(channel: SocketChannel) {
            val pipeline = channel.pipeline()
            val sourceCodec = HttpServerCodec()
            val connection = DefaultHttp2Connection(true)

            val listener = InboundHttp2ToHttpAdapterBuilder(connection)
                .propagateSettings(true)
                .maxContentLength(MAX_VALUE)
                .build()

            val http2Handler = HttpToHttp2ConnectionHandlerBuilder()
                .connection(connection)
                .frameListener(
                    listener
                )
                .build()

            val upgradeCodecFactory =
                UpgradeCodecFactory { protocol ->
                    if (AsciiString.contentEquals(
                            Http2CodecUtil.HTTP_UPGRADE_PROTOCOL_NAME,
                            protocol
                        )
                    ) {
                        Http2ServerUpgradeCodec(http2Handler)
                    } else {
                        null
                    }
                }

            val upgradeHandler = HttpServerUpgradeHandler(sourceCodec, upgradeCodecFactory)

            val cleartextHttp2ServerUpgradeHandler = CleartextHttp2ServerUpgradeHandler(
                sourceCodec, upgradeHandler,
                http2Handler
            )

            pipeline.addLast(cleartextHttp2ServerUpgradeHandler)
            pipeline.addLast(HttpHandler())
        }
    }
}
