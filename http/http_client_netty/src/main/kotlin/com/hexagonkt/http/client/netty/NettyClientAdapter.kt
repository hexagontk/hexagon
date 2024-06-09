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
import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled.EMPTY_BUFFER
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.HttpResponseStatus.*
import io.netty.handler.codec.http.HttpVersion.HTTP_1_1
import io.netty.handler.codec.http2.*
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

        channel.writeAndFlush(createRequest(host, port, request)).sync()
        channel.closeFuture().sync()

        val context = channel.pipeline().context(HttpClientResponseHandler::class.java)
        val responseHandler = context.handler() as HttpClientResponseHandler
        return createResponse(responseHandler.response)
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

    private fun createRequest(host: String, port: Int, request: HttpRequestPort): FullHttpRequest {
        val nettyMethod = when (request.method) {
            HttpMethod.GET -> NettyMethod.GET
            HttpMethod.POST -> NettyMethod.POST
            HttpMethod.PUT -> NettyMethod.PUT
            HttpMethod.DELETE -> NettyMethod.DELETE
            HttpMethod.HEAD -> NettyMethod.HEAD
            HttpMethod.PATCH -> NettyMethod.PATCH
            HttpMethod.OPTIONS -> NettyMethod.OPTIONS
            HttpMethod.TRACE -> NettyMethod.TRACE
        }

        val nettyRequest = DefaultFullHttpRequest(HTTP_1_1, nettyMethod, request.path, EMPTY_BUFFER)
        nettyRequest.headers().add(HttpHeaderNames.HOST, "$host:$port")
//        nettyRequest.headers().add(SCHEME.text(), HttpScheme.HTTPS)
//        nettyRequest.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP)
//        nettyRequest.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.DEFLATE)
        return nettyRequest
    }

    private fun createResponse(response: FullHttpResponse): HttpResponsePort {
        val content = response.content()

        val body = if (content.isReadable) ByteBufUtil.getBytes(content) else byteArrayOf()
        return HttpResponse(
            status = nettyStatus(response.status()),
            body = String(body),
        )
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

    internal fun nettyStatus(status: HttpResponseStatus): HttpStatus =
        when (status) {
            CONTINUE -> CONTINUE_100
            SWITCHING_PROTOCOLS -> SWITCHING_PROTOCOLS_101
            PROCESSING -> PROCESSING_102

            OK -> OK_200
            CREATED -> CREATED_201
            ACCEPTED -> ACCEPTED_202
            NON_AUTHORITATIVE_INFORMATION -> NON_AUTHORITATIVE_INFORMATION_203
            NO_CONTENT -> NO_CONTENT_204
            RESET_CONTENT -> RESET_CONTENT_205
            PARTIAL_CONTENT -> PARTIAL_CONTENT_206
            MULTI_STATUS -> MULTI_STATUS_207

            MULTIPLE_CHOICES -> MULTIPLE_CHOICES_300
            MOVED_PERMANENTLY -> MOVED_PERMANENTLY_301
            FOUND -> FOUND_302
            SEE_OTHER -> SEE_OTHER_303
            NOT_MODIFIED -> NOT_MODIFIED_304
            USE_PROXY -> USE_PROXY_305
            TEMPORARY_REDIRECT -> TEMPORARY_REDIRECT_307
            PERMANENT_REDIRECT -> PERMANENT_REDIRECT_308

            BAD_REQUEST -> BAD_REQUEST_400
            NOT_FOUND -> NOT_FOUND_404
            UNAUTHORIZED -> UNAUTHORIZED_401
            PAYMENT_REQUIRED -> PAYMENT_REQUIRED_402
            FORBIDDEN -> FORBIDDEN_403
            METHOD_NOT_ALLOWED -> METHOD_NOT_ALLOWED_405
            NOT_ACCEPTABLE -> NOT_ACCEPTABLE_406
            PROXY_AUTHENTICATION_REQUIRED -> PROXY_AUTHENTICATION_REQUIRED_407
            REQUEST_TIMEOUT -> REQUEST_TIMEOUT_408
            CONFLICT -> CONFLICT_409
            GONE -> GONE_410
            LENGTH_REQUIRED -> LENGTH_REQUIRED_411
            PRECONDITION_FAILED -> PRECONDITION_FAILED_412
            REQUEST_URI_TOO_LONG -> URI_TOO_LONG_414
            UNSUPPORTED_MEDIA_TYPE -> UNSUPPORTED_MEDIA_TYPE_415
            REQUESTED_RANGE_NOT_SATISFIABLE -> RANGE_NOT_SATISFIABLE_416
            EXPECTATION_FAILED -> EXPECTATION_FAILED_417
            MISDIRECTED_REQUEST -> MISDIRECTED_REQUEST_421
            UNPROCESSABLE_ENTITY -> UNPROCESSABLE_CONTENT_422
            LOCKED -> LOCKED_423
            FAILED_DEPENDENCY -> FAILED_DEPENDENCY_424
            UPGRADE_REQUIRED -> UPGRADE_REQUIRED_426
            PRECONDITION_REQUIRED -> PRECONDITION_REQUIRED_428
            TOO_MANY_REQUESTS -> TOO_MANY_REQUESTS_429
            REQUEST_HEADER_FIELDS_TOO_LARGE -> REQUEST_HEADER_FIELDS_TOO_LARGE_431

            INTERNAL_SERVER_ERROR -> INTERNAL_SERVER_ERROR_500
            NOT_IMPLEMENTED -> NOT_IMPLEMENTED_501
            BAD_GATEWAY -> BAD_GATEWAY_502
            SERVICE_UNAVAILABLE -> SERVICE_UNAVAILABLE_503
            GATEWAY_TIMEOUT -> GATEWAY_TIMEOUT_504
            HTTP_VERSION_NOT_SUPPORTED -> HTTP_VERSION_NOT_SUPPORTED_505
            VARIANT_ALSO_NEGOTIATES -> VARIANT_ALSO_NEGOTIATES_506
            INSUFFICIENT_STORAGE -> INSUFFICIENT_STORAGE_507
            NOT_EXTENDED -> NOT_EXTENDED_510
            NETWORK_AUTHENTICATION_REQUIRED -> NETWORK_AUTHENTICATION_REQUIRED_511

            else -> HttpStatus(status.code())
        }
}
