package com.hexagonkt.http.client.netty

import com.hexagonkt.core.security.loadKeyStore
import com.hexagonkt.http.SslSettings
import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.client.HttpClientSettings
import com.hexagonkt.http.handlers.HttpHandler
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.HttpResponse
import com.hexagonkt.http.model.ws.WsSession
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http2.*
import io.netty.handler.codec.http2.HttpConversionUtil.ExtensionHeaderNames.SCHEME
import io.netty.handler.ssl.*
import io.netty.handler.ssl.ApplicationProtocolConfig.Protocol.ALPN
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE
import io.netty.handler.ssl.ClientAuth.OPTIONAL
import io.netty.handler.ssl.ClientAuth.REQUIRE
import io.netty.handler.ssl.SslProvider.JDK
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import io.netty.util.concurrent.DefaultEventExecutorGroup
import java.util.concurrent.Flow.Publisher
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.SECONDS
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.TrustManagerFactory
import io.netty.handler.codec.http.HttpMethod as NettyMethod

/**
 * Client to use other REST services.
 */
open class NettyClientAdapter(
    private val bossGroupThreads: Int = 1,
    private val soBacklog: Int = 4 * 1_024,
    private val soReuseAddr: Boolean = true,
    private val soKeepAlive: Boolean = true,
) : HttpClientPort {

    protected lateinit var httpClient: HttpClient
    private lateinit var httpSettings: HttpClientSettings
    private var started: Boolean = false

    private lateinit var nettyClient: Bootstrap
    private lateinit var initializer: Http2ClientInitializer
    private lateinit var bossGroup: MultithreadEventLoopGroup

    constructor() : this(
        bossGroupThreads = 1,
        soBacklog = 4 * 1_024,
        soReuseAddr = true,
        soKeepAlive = true,
    )

    override fun startUp(client: HttpClient) {
        val settings = client.settings

        httpClient = client
        httpSettings = settings

        bossGroup = groupSupplier(bossGroupThreads)
        initializer = Http2ClientInitializer(createSSLContext(), Int.MAX_VALUE)
        nettyClient = clientBootstrapSupplier(bossGroup).handler(initializer)

        started = true
    }

    override fun shutDown() {
        bossGroup.shutdownGracefully()

        started = false
    }

    override fun started() =
        started

    override fun send(request: HttpRequestPort): HttpResponsePort {

        val base = httpSettings.baseUrl
        val host = base?.host ?: request.host
        val port = base?.port ?: request.port

        try {
            val channel = nettyClient
                .remoteAddress(host, port)
                .connect().syncUninterruptibly().channel()
            val http2SettingsHandler = initializer.settingsHandler
            http2SettingsHandler.awaitSettings(60, SECONDS)

            val responseHandler = initializer.responseHandler
            val streamId = 3
            val nettyRequest = createGetRequest(host, port)
            responseHandler.put(streamId, channel.write(nettyRequest), channel.newPromise())
            channel.flush()

            val response = responseHandler.awaitResponses(60, SECONDS)
        } finally {
        }

        return HttpResponse()
    }

    private fun createInitializer(
        sslSettings: SslSettings?,
        handlers: Map<HttpMethod, HttpHandler>,
        group: DefaultEventExecutorGroup?,
        settings: HttpClientSettings
    ) =
        when {
            sslSettings != null -> sslInitializer(sslSettings, group, settings)
            else -> HttpChannelInitializer(
                group,
            )
        }

    private fun sslInitializer(
        sslSettings: SslSettings,
        group: DefaultEventExecutorGroup?,
        settings: HttpClientSettings
    ): HttpsChannelInitializer =
        HttpsChannelInitializer(
            sslContext(sslSettings),
            sslSettings,
            group,
        )

    private fun sslContext(sslSettings: SslSettings): SslContext {
        val keyManager = createKeyManagerFactory(sslSettings)

        val sslContextBuilder = SslContextBuilder
            .forServer(keyManager)
            .clientAuth(if (sslSettings.clientAuth) REQUIRE else OPTIONAL)

        val trustManager = createTrustManagerFactory(sslSettings)

        return if (trustManager == null) sslContextBuilder.build()
        else sslContextBuilder.trustManager(trustManager).build()
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

    private fun createGetRequest(host: String, port: Int): FullHttpRequest {
        val httpVersion = HttpVersion.valueOf("HTTP/2.0")
        val request = DefaultFullHttpRequest(httpVersion, NettyMethod.GET, "/", Unpooled.EMPTY_BUFFER)
        request.headers().add(HttpHeaderNames.HOST, "$host:$port")
        request.headers().add(SCHEME.text(), HttpScheme.HTTPS)
        request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP)
        request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.DEFLATE)
        return request
    }

    private fun createSSLContext(): SslContext {

        val sslCtx = SslContextBuilder.forClient()
            .sslProvider(JDK)
            .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
            .trustManager(InsecureTrustManagerFactory.INSTANCE)
            .applicationProtocolConfig(
                ApplicationProtocolConfig(
                    ALPN,
                    NO_ADVERTISE,
                    ACCEPT, ApplicationProtocolNames.HTTP_2
                )
            )
            .build()

        return sslCtx
    }

    open fun clientBootstrapSupplier(bossGroup: MultithreadEventLoopGroup): Bootstrap =
        Bootstrap()
            .group(bossGroup)
            .channel(NioSocketChannel::class.java)
            .option(ChannelOption.SO_BACKLOG, soBacklog)
            .option(ChannelOption.SO_REUSEADDR, soReuseAddr)
            .option(ChannelOption.SO_KEEPALIVE, soKeepAlive)
            .option(ChannelOption.SO_REUSEADDR, soReuseAddr)

    open fun groupSupplier(it: Int): MultithreadEventLoopGroup =
        NioEventLoopGroup(it)

    override fun ws(
        path: String,
        onConnect: WsSession.() -> Unit,
        onBinary: WsSession.(data: ByteArray) -> Unit,
        onText: WsSession.(text: String) -> Unit,
        onPing: WsSession.(data: ByteArray) -> Unit,
        onPong: WsSession.(data: ByteArray) -> Unit,
        onClose: WsSession.(status: Int, reason: String) -> Unit,
    ): WsSession {
        throw UnsupportedOperationException("WebSockets not supported. Use 'http_client_netty_ws")
    }

    override fun sse(request: HttpRequestPort): Publisher<ServerEvent> {
        throw UnsupportedOperationException("Not supported")
    }
}

internal class Http2ClientInitializer(
    private val sslCtx: SslContext?,
    private val maxContentLength: Int = 0,
) : ChannelInitializer<SocketChannel>() {

    lateinit var settingsHandler: Http2SettingsHandler
    lateinit var responseHandler: Http2ClientResponseHandler

    override fun initChannel(ch: SocketChannel) {
        settingsHandler = Http2SettingsHandler(ch.newPromise())
        responseHandler = Http2ClientResponseHandler()

        if (sslCtx != null) {
            val pipeline: ChannelPipeline = ch.pipeline()
            pipeline.addLast(sslCtx.newHandler(ch.alloc()))
            pipeline.addLast(
                getClientAPNHandler(maxContentLength, settingsHandler, responseHandler)
            )
        }
    }

    private fun getClientAPNHandler(
        maxContentLength: Int,
        settingsHandler: Http2SettingsHandler?,
        responseHandler: Http2ClientResponseHandler?
    ): ApplicationProtocolNegotiationHandler {
        val connection: Http2Connection = DefaultHttp2Connection(false)

        val connectionHandler = HttpToHttp2ConnectionHandlerBuilder()
            .frameListener(
                DelegatingDecompressorFrameListener(
                    connection,
                    InboundHttp2ToHttpAdapterBuilder(connection).maxContentLength(maxContentLength)
                        .propagateSettings(true)
                        .build()
                )
            )
            .connection(connection)
            .build()

        val clientAPNHandler: ApplicationProtocolNegotiationHandler =
            object : ApplicationProtocolNegotiationHandler(ApplicationProtocolNames.HTTP_2) {
                override fun configurePipeline(ctx: ChannelHandlerContext, protocol: String) {
                    if (ApplicationProtocolNames.HTTP_2 == protocol) {
                        val p = ctx.pipeline()
                        p.addLast(connectionHandler)
                        p.addLast(settingsHandler, responseHandler)
                        return
                    }
                    ctx.close()
                    throw IllegalStateException("Protocol: $protocol not supported")
                }
            }

        return clientAPNHandler
    }
}

class Http2SettingsHandler(private val promise: ChannelPromise) :
    SimpleChannelInboundHandler<Http2Settings>() {
    fun awaitSettings(timeout: Long, unit: TimeUnit?) {
        check(promise.awaitUninterruptibly(timeout, unit)) { "Timed out waiting for settings" }
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: Http2Settings) {
        promise.setSuccess()

        ctx.pipeline().remove(this)
    }
}
