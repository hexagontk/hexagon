package com.hexagonkt.http.client.netty

import com.hexagonkt.core.security.createKeyManagerFactory
import com.hexagonkt.core.security.createTrustManagerFactory
import com.hexagonkt.http.SslSettings
import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.client.HttpClientSettings
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
import io.netty.handler.ssl.ApplicationProtocolNames.HTTP_2
import io.netty.handler.ssl.ClientAuth.OPTIONAL
import io.netty.handler.ssl.ClientAuth.REQUIRE
import io.netty.handler.ssl.SslProvider.JDK
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import java.util.concurrent.Flow.Publisher
import io.netty.handler.codec.http.HttpMethod as NettyMethod

/**
 * Client to use other REST services.
 */
open class NettyClientAdapter(
    private val bossGroupThreads: Int = 1,
    private val soReuseAddr: Boolean = true,
    private val soKeepAlive: Boolean = true,
) : HttpClientPort {

    protected lateinit var httpClient: HttpClient
    private lateinit var httpSettings: HttpClientSettings
    private var started: Boolean = false

    private lateinit var nettyClient: Bootstrap
    private lateinit var bossGroup: EventLoopGroup

    constructor() : this(
        bossGroupThreads = 1,
        soReuseAddr = true,
        soKeepAlive = true,
    )

    override fun startUp(client: HttpClient) {
        httpClient = client
        httpSettings = client.settings
        bossGroup = groupSupplier(bossGroupThreads)

        val initializer = createInitializer(httpSettings.sslSettings, bossGroup)
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
        val channel = nettyClient.connect(host, port).sync().channel()
        val nettyRequest = createRequest(host, port)

        channel.writeAndFlush(nettyRequest)
        channel.closeFuture().sync()
        val c = channel.pipeline().context(HttpClientResponseHandler::class.java).handler() as HttpClientResponseHandler

        return HttpResponse()
    }

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
        throw UnsupportedOperationException("SSE not supported")
    }

    open fun groupSupplier(it: Int): EventLoopGroup =
        NioEventLoopGroup(it)

    open fun clientBootstrapSupplier(bossGroup: EventLoopGroup): Bootstrap =
        Bootstrap()
            .group(bossGroup)
            .channel(NioSocketChannel::class.java)
            .option(ChannelOption.SO_REUSEADDR, soReuseAddr)
            .option(ChannelOption.SO_KEEPALIVE, soKeepAlive)
            .option(ChannelOption.SO_REUSEADDR, soReuseAddr)

    private fun createInitializer(
        sslSettings: SslSettings?, group: EventLoopGroup?
    ): ChannelInitializer<SocketChannel> =
        when {
            sslSettings != null -> sslInitializer(sslSettings, group)
            else -> HttpChannelInitializer(group)
        }

    private fun sslInitializer(
        sslSettings: SslSettings, group: EventLoopGroup?
    ): HttpsChannelInitializer =
        HttpsChannelInitializer(sslContext(sslSettings), group)

    private fun createRequest(host: String, port: Int): FullHttpRequest {
        val httpVersion = HttpVersion.valueOf("HTTP/1.1")
        val request = DefaultFullHttpRequest(httpVersion, NettyMethod.GET, "/", Unpooled.EMPTY_BUFFER)
        request.headers().add(HttpHeaderNames.HOST, "$host:$port")
        request.headers().add(SCHEME.text(), HttpScheme.HTTPS)
        request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP)
        request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.DEFLATE)
        return request
    }

    private fun sslContext(sslSettings: SslSettings): SslContext {
        val keyStoreUrl = sslSettings.keyStore ?: error("")
        val keyManager = createKeyManagerFactory(keyStoreUrl, sslSettings.keyStorePassword)
        val password = sslSettings.trustStorePassword
        val trustManager = sslSettings.trustStore?.let { createTrustManagerFactory(it, password) }
        val sslContextBuilder = SslContextBuilder
            .forClient()
            .sslProvider(JDK)
            .keyManager(keyManager)
            .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
            .clientAuth(if (sslSettings.clientAuth) REQUIRE else OPTIONAL)
            .trustManager(InsecureTrustManagerFactory.INSTANCE)
            .applicationProtocolConfig(
                ApplicationProtocolConfig(ALPN, NO_ADVERTISE, ACCEPT, HTTP_2)
            )

        return if (trustManager == null) sslContextBuilder.build()
        else sslContextBuilder.trustManager(trustManager).build()
    }
}
