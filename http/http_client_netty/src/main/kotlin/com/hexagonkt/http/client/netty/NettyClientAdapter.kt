package com.hexagonkt.http.client.netty

import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.client.HttpClientSettings
import com.hexagonkt.http.model.*
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
import io.netty.handler.codec.http2.HttpConversionUtil.ExtensionHeaderNames.STREAM_ID
import io.netty.handler.ssl.*
import io.netty.handler.ssl.ApplicationProtocolConfig.Protocol.ALPN
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE
import io.netty.handler.ssl.SslProvider.JDK
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import io.netty.util.CharsetUtil
import java.util.concurrent.Flow.Publisher
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.collections.Map.Entry
import io.netty.handler.codec.http.HttpMethod as NettyMethod

/**
 * Client to use other REST services.
 */
open class NettyClientAdapter : HttpClientPort {

    protected lateinit var httpClient: HttpClient
    private lateinit var httpSettings: HttpClientSettings
    private var started: Boolean = false

    override fun startUp(client: HttpClient) {
        val settings = client.settings

        httpClient = client
        httpSettings = settings

        started = true
    }

    override fun shutDown() {
        started = false
    }

    override fun started() =
        started

    override fun send(request: HttpRequestPort): HttpResponsePort {

        val HOST = ""
        val PORT = 0
        val workerGroup = NioEventLoopGroup()
        val initializer = Http2ClientInitializer(createSSLContext(), Int.MAX_VALUE, HOST, PORT)

        try {
            val b = Bootstrap()
            b.group(workerGroup)
            b.channel(NioSocketChannel::class.java)
            b.option(ChannelOption.SO_KEEPALIVE, true)
            b.remoteAddress(HOST, PORT)
            b.handler(initializer)

            val channel = b.connect().syncUninterruptibly().channel()

            val http2SettingsHandler = initializer.settingsHandler
            http2SettingsHandler.awaitSettings(60, SECONDS)

            val request: FullHttpRequest = createGetRequest(HOST, PORT)

            val responseHandler = initializer.responseHandler
            val streamId = 3

            responseHandler.put(streamId, channel.write(request), channel.newPromise())
            channel.flush()

            val response = responseHandler.awaitResponses(60, SECONDS)
        } finally {
            workerGroup.shutdownGracefully()
        }

        throw UnsupportedOperationException("Not supported")
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
        throw UnsupportedOperationException("WebSockets not supported. Use 'http_client_jetty_ws")
    }

    override fun sse(request: HttpRequestPort): Publisher<ServerEvent> {
        throw UnsupportedOperationException("Not supported")
    }
}

internal class Http2ClientInitializer(
    private val sslCtx: SslContext?,
    private val maxContentLength: Int = 0,
    private val host: String? = null,
    private val port: Int = 0
) : ChannelInitializer<SocketChannel>() {

    lateinit var settingsHandler: Http2SettingsHandler
    lateinit var responseHandler: Http2ClientResponseHandler

    override fun initChannel(ch: SocketChannel) {
        settingsHandler = Http2SettingsHandler(ch.newPromise())
        responseHandler = Http2ClientResponseHandler()

        if (sslCtx != null) {
            val pipeline: ChannelPipeline = ch.pipeline()
            pipeline.addLast(sslCtx.newHandler(ch.alloc(), host, port))
            pipeline.addLast(
                getClientAPNHandler(maxContentLength, settingsHandler, responseHandler)
            )
        }
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

class Http2ClientResponseHandler : SimpleChannelInboundHandler<FullHttpResponse>() {
    private val streamIdMap: MutableMap<Int, MapValues> = HashMap()

    fun put(streamId: Int, writeFuture: ChannelFuture, promise: ChannelPromise): MapValues? {
        return streamIdMap.put(streamId, MapValues(writeFuture, promise))
    }

    fun awaitResponses(timeout: Long, unit: TimeUnit): String? {
        val itr: MutableIterator<Entry<Int, MapValues>> = streamIdMap.entries.iterator()

        var response: String? = null

        while (itr.hasNext()) {
            val entry = itr.next()
            val writeFuture = entry.value.writeFuture

            check(
                writeFuture.awaitUninterruptibly(
                    timeout,
                    unit
                )
            ) { "Timed out waiting to write for stream id " + entry.key }
            if (!writeFuture.isSuccess) {
                throw RuntimeException(writeFuture.cause())
            }
            val promise = entry.value.promise

            check(promise.awaitUninterruptibly(timeout, unit)) {
                "Timed out waiting for response on stream id " + entry.key
            }
            if (!promise.isSuccess) {
                throw RuntimeException(promise.cause())
            }
            response = entry.value.response

            itr.remove()
        }

        return response
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: FullHttpResponse) {
        val streamId = msg.headers().getInt(STREAM_ID.text()) ?: return

        val value = streamIdMap[streamId]

        if (value == null) {
            ctx.close()
        } else {
            val content = msg.content()
            if (content.isReadable) {
                val contentLength = content.readableBytes()
                val arr = ByteArray(contentLength)
                content.readBytes(arr)
                val response = String(arr, 0, contentLength, CharsetUtil.UTF_8)
                value.response = response
            }

            value.promise
                .setSuccess()
        }
    }

    class MapValues(var writeFuture: ChannelFuture, var promise: ChannelPromise) {
        var response: String? = null
    }
}

fun createSSLContext(): SslContext {

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

fun getClientAPNHandler(
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

fun createGetRequest(host: String, port: Int): FullHttpRequest {
    val httpVersion = HttpVersion.valueOf("HTTP/2.0")
    val request = DefaultFullHttpRequest(httpVersion, NettyMethod.GET, "/", Unpooled.EMPTY_BUFFER)
    request.headers().add(HttpHeaderNames.HOST, "$host:$port")
    request.headers().add(SCHEME.text(), HttpScheme.HTTPS)
    request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP)
    request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.DEFLATE)
    return request
}
