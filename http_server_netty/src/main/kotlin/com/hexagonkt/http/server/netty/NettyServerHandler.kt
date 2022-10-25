package com.hexagonkt.http.server.netty

import com.hexagonkt.http.bodyToBytes
import com.hexagonkt.http.model.*
import com.hexagonkt.http.server.handlers.PathHandler
import com.hexagonkt.http.server.model.HttpServerResponse
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener.CLOSE
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.HttpHeaderNames.*
import io.netty.handler.codec.http.HttpHeaderValues.CHUNKED
import io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpMethod.GET
import io.netty.handler.codec.http.HttpResponseStatus.*
import io.netty.handler.codec.http.HttpVersion.HTTP_1_1
import io.netty.handler.codec.http.cookie.DefaultCookie
import io.netty.handler.codec.http.cookie.ServerCookieEncoder.STRICT
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory
import io.netty.handler.ssl.SslHandler
import io.netty.handler.ssl.SslHandshakeCompletionEvent
import java.net.InetSocketAddress
import java.security.cert.X509Certificate
import java.util.concurrent.Flow.*

internal class NettyServerHandler(
    private val handlers: Map<HttpMethod, PathHandler>,
    private val sslHandler: SslHandler?,
) : ChannelInboundHandlerAdapter() {

    private var certificates: List<X509Certificate> = emptyList()

    override fun channelRead(context: ChannelHandlerContext, nettyRequest: Any) {
        if (nettyRequest !is FullHttpRequest)
            return

        val result = nettyRequest.decoderResult()

        if (result.isFailure)
            throw IllegalStateException(result.cause())

        val channel = context.channel()
        val address = channel.remoteAddress() as InetSocketAddress
        val method = nettyRequest.method()
        val headers = nettyRequest.headers()
        val request = NettyRequestAdapter(method, nettyRequest, certificates, address, headers)
        val response = handlers[method]?.process(request) ?: HttpServerResponse()

        val body = response.body
        val connection = headers[CONNECTION]?.lowercase()
        val upgrade = headers[UPGRADE]?.lowercase()

        val isSse = body is Publisher<*>
        val status = response.status
        val isWebSocket = connection == "upgrade"
            && upgrade == "websocket"
            && method == GET
            && status == SuccessStatus.ACCEPTED

        when {
            isSse -> handleSse(context, response, body)
            isWebSocket -> handleWebSocket(context, request, response, nettyRequest, channel)
            else -> writeResponse(context, response, HttpUtil.isKeepAlive(nettyRequest))
        }
    }

    @Suppress("UNCHECKED_CAST") // Body not cast to Publisher<HttpServerEvent> due to type erasure
    private fun handleSse(context: ChannelHandlerContext, response: HttpServerResponse, body: Any) {
        val status = nettyStatus(response.status)
        val nettyResponse = DefaultHttpResponse(HTTP_1_1, status)
        val headers = nettyResponse.headers()

        val hexagonHeaders = response.headers
        if (hexagonHeaders.httpFields.isNotEmpty())
            hexagonHeaders.values.map { (k, v) -> headers.add(k, v) }

        val hexagonCookies = response.cookies
        if (hexagonCookies.isNotEmpty())
            headers[SET_COOKIE] = STRICT.encode(nettyCookies(hexagonCookies))

        val contentType = response.contentType
        if (contentType != null)
            headers[CONTENT_TYPE] = contentType.text

        headers[TRANSFER_ENCODING] = CHUNKED
        headers[CONNECTION] = KEEP_ALIVE
        context.writeAndFlush(nettyResponse)

        // TODO Close when publisher is done
        val publisher = body as Publisher<ServerEvent>
        publisher.subscribe(object : Subscriber<ServerEvent> {
            override fun onError(throwable: Throwable) {}

            override fun onComplete() {}

            override fun onSubscribe(subscription: Subscription) {
                subscription.request(Long.MAX_VALUE)
            }

            override fun onNext(item: ServerEvent) {
                val eventData = Unpooled.copiedBuffer(item.eventData.toByteArray())
                context.writeAndFlush(DefaultHttpContent(eventData))
            }
        })
    }

    private fun handleWebSocket(
        context: ChannelHandlerContext,
        request: NettyRequestAdapter,
        response: HttpServerResponse,
        nettyRequest: FullHttpRequest,
        channel: Channel
    ) {
        val session = NettyWsSession(context, request)
        val nettyWebSocketHandler = NettyWebSocketHandler(
            session,
            response.onBinary,
            response.onText,
            response.onPing,
            response.onPong,
            response.onClose,
        )

        context.pipeline().replace(this, "webSocketHandler", nettyWebSocketHandler)
        wsHandshake(nettyRequest, channel)
        session.(response.onConnect)()
    }

    private fun wsHandshake(nettyRequest: FullHttpRequest, channel: Channel) {
        val host = nettyRequest.headers()["host"]
        val uri = nettyRequest.uri()
        val url = "ws://$host$uri"
        val wsFactory = WebSocketServerHandshakerFactory(url, null, true)
        val handShaker = wsFactory.newHandshaker(nettyRequest)

        if (handShaker == null)
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(channel)
        else
            handShaker.handshake(channel, nettyRequest)
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
        if (evt is SslHandshakeCompletionEvent && sslHandler != null) {
            val peerCertificates = sslHandler.engine().session.peerCertificates
            certificates = peerCertificates.map { it as X509Certificate }
        }
    }

    @Suppress("OVERRIDE_DEPRECATION") // Deprecated in base interface, but allowed in parent class
    override fun exceptionCaught(context: ChannelHandlerContext, cause: Throwable) {
        val body = "Failure: $cause\n"
        val response = HttpServerResponse(body, status = ServerErrorStatus.INTERNAL_SERVER_ERROR)
        writeResponse(context, response, false)
    }

    private fun writeResponse(
        context: ChannelHandlerContext,
        hexagonResponse: HttpServerResponse,
        keepAlive: Boolean,
    ) {

        val buffer = Unpooled.copiedBuffer(bodyToBytes(hexagonResponse.body))
        val status = nettyStatus(hexagonResponse.status)
        val response = DefaultFullHttpResponse(HTTP_1_1, status, buffer)
        val headers = response.headers()

        val hexagonHeaders = hexagonResponse.headers
        if (hexagonHeaders.httpFields.isNotEmpty())
            hexagonHeaders.values.map { (k, v) -> headers.add(k, v) }

        val hexagonCookies = hexagonResponse.cookies
        if (hexagonCookies.isNotEmpty())
            headers[SET_COOKIE] = STRICT.encode(nettyCookies(hexagonCookies))

        val contentType = hexagonResponse.contentType
        if (contentType != null)
            headers[CONTENT_TYPE] = contentType.text

        if (keepAlive) {
            headers.setInt(CONTENT_LENGTH, response.content().readableBytes())
            headers[CONNECTION] = KEEP_ALIVE
            context.writeAndFlush(response)
        }
        else {
            context.writeAndFlush(response).addListener(CLOSE)
        }
    }

    private fun nettyCookies(hexagonCookies: List<HttpCookie>) =
        hexagonCookies.map {
            DefaultCookie(it.name, it.value).apply {
                if (it.maxAge != -1L)
                    setMaxAge(it.maxAge)
                isSecure = it.secure
            }
        }

    internal fun nettyStatus(status: HttpStatus): HttpResponseStatus =
        when (status) {
            InformationStatus.CONTINUE -> CONTINUE
            InformationStatus.SWITCHING_PROTOCOLS -> SWITCHING_PROTOCOLS
            InformationStatus.PROCESSING -> PROCESSING

            SuccessStatus.OK -> OK
            SuccessStatus.CREATED -> CREATED
            ACCEPTED -> ACCEPTED
            SuccessStatus.NON_AUTHORITATIVE_INFORMATION -> NON_AUTHORITATIVE_INFORMATION
            SuccessStatus.NO_CONTENT -> NO_CONTENT
            SuccessStatus.RESET_CONTENT -> RESET_CONTENT
            SuccessStatus.PARTIAL_CONTENT -> PARTIAL_CONTENT
            SuccessStatus.MULTI_STATUS -> MULTI_STATUS

            RedirectionStatus.MULTIPLE_CHOICES -> MULTIPLE_CHOICES
            RedirectionStatus.MOVED_PERMANENTLY -> MOVED_PERMANENTLY
            RedirectionStatus.FOUND -> FOUND
            RedirectionStatus.SEE_OTHER -> SEE_OTHER
            RedirectionStatus.NOT_MODIFIED -> NOT_MODIFIED
            RedirectionStatus.USE_PROXY -> USE_PROXY
            RedirectionStatus.TEMPORARY_REDIRECT -> TEMPORARY_REDIRECT
            RedirectionStatus.PERMANENT_REDIRECT -> PERMANENT_REDIRECT

            ClientErrorStatus.BAD_REQUEST -> BAD_REQUEST
            ClientErrorStatus.NOT_FOUND -> NOT_FOUND
            ClientErrorStatus.UNAUTHORIZED -> UNAUTHORIZED
            ClientErrorStatus.PAYMENT_REQUIRED -> PAYMENT_REQUIRED
            ClientErrorStatus.FORBIDDEN -> FORBIDDEN
            ClientErrorStatus.METHOD_NOT_ALLOWED -> METHOD_NOT_ALLOWED
            ClientErrorStatus.NOT_ACCEPTABLE -> NOT_ACCEPTABLE
            ClientErrorStatus.PROXY_AUTHENTICATION_REQUIRED -> PROXY_AUTHENTICATION_REQUIRED
            ClientErrorStatus.REQUEST_TIMEOUT -> REQUEST_TIMEOUT
            ClientErrorStatus.CONFLICT -> CONFLICT
            ClientErrorStatus.GONE -> GONE
            ClientErrorStatus.LENGTH_REQUIRED -> LENGTH_REQUIRED
            ClientErrorStatus.PRECONDITION_FAILED -> PRECONDITION_FAILED
            ClientErrorStatus.URI_TOO_LONG -> REQUEST_URI_TOO_LONG
            ClientErrorStatus.UNSUPPORTED_MEDIA_TYPE -> UNSUPPORTED_MEDIA_TYPE
            ClientErrorStatus.RANGE_NOT_SATISFIABLE -> REQUESTED_RANGE_NOT_SATISFIABLE
            ClientErrorStatus.EXPECTATION_FAILED -> EXPECTATION_FAILED
            ClientErrorStatus.MISDIRECTED_REQUEST -> MISDIRECTED_REQUEST
            ClientErrorStatus.UNPROCESSABLE_CONTENT -> UNPROCESSABLE_ENTITY
            ClientErrorStatus.LOCKED -> LOCKED
            ClientErrorStatus.FAILED_DEPENDENCY -> FAILED_DEPENDENCY
            ClientErrorStatus.UPGRADE_REQUIRED -> UPGRADE_REQUIRED
            ClientErrorStatus.PRECONDITION_REQUIRED -> PRECONDITION_REQUIRED
            ClientErrorStatus.TOO_MANY_REQUESTS -> TOO_MANY_REQUESTS
            ClientErrorStatus.REQUEST_HEADER_FIELDS_TOO_LARGE -> REQUEST_HEADER_FIELDS_TOO_LARGE

            ServerErrorStatus.INTERNAL_SERVER_ERROR -> INTERNAL_SERVER_ERROR
            ServerErrorStatus.NOT_IMPLEMENTED -> NOT_IMPLEMENTED
            ServerErrorStatus.BAD_GATEWAY -> BAD_GATEWAY
            ServerErrorStatus.SERVICE_UNAVAILABLE -> SERVICE_UNAVAILABLE
            ServerErrorStatus.GATEWAY_TIMEOUT -> GATEWAY_TIMEOUT
            ServerErrorStatus.HTTP_VERSION_NOT_SUPPORTED -> HTTP_VERSION_NOT_SUPPORTED
            ServerErrorStatus.VARIANT_ALSO_NEGOTIATES -> VARIANT_ALSO_NEGOTIATES
            ServerErrorStatus.INSUFFICIENT_STORAGE -> INSUFFICIENT_STORAGE
            ServerErrorStatus.NOT_EXTENDED -> NOT_EXTENDED
            ServerErrorStatus.NETWORK_AUTHENTICATION_REQUIRED -> NETWORK_AUTHENTICATION_REQUIRED

            else -> HttpResponseStatus(status.code, status.toString())
        }
}
