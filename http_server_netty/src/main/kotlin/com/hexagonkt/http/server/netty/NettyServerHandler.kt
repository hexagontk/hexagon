package com.hexagonkt.http.server.netty

import com.hexagonkt.handlers.Context
import com.hexagonkt.http.bodyToBytes
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.Cookie
import com.hexagonkt.http.handlers.HttpHandler
import com.hexagonkt.http.handlers.HttpContext
import com.hexagonkt.http.model.HttpCall
import com.hexagonkt.http.model.HttpResponse
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
import io.netty.handler.codec.http.HttpRequest
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
    private val handlers: Map<HttpMethod, HttpHandler>,
    private val sslHandler: SslHandler?,
) : ChannelInboundHandlerAdapter() {

    private var certificates: List<X509Certificate> = emptyList()

    override fun channelRead(context: ChannelHandlerContext, nettyRequest: Any) {
        if (nettyRequest is HttpRequest)
            readHttpRequest(context, nettyRequest)
    }

    private fun readHttpRequest(context: ChannelHandlerContext, nettyRequest: HttpRequest) {
        val result = nettyRequest.decoderResult()

        if (result.isFailure)
            throw IllegalStateException(result.cause())

        val channel = context.channel()
        val address = channel.remoteAddress() as InetSocketAddress
        val method = nettyRequest.method()
        val pathHandler = handlers[method]

        if (pathHandler == null) {
            writeResponse(context, HttpResponse(), HttpUtil.isKeepAlive(nettyRequest))
            return
        }

        val headers = nettyRequest.headers()
        val request = NettyRequestAdapter(method, nettyRequest, certificates, address, headers)

        val resultContext = pathHandler.process(request)
        val response = resultContext.event.response

        val body = response.body
        val connection = headers[CONNECTION]?.lowercase()
        val upgrade = headers[UPGRADE]?.lowercase()

        val isSse = body is Publisher<*>
        val isWebSocket = connection == "upgrade"
            && upgrade == "websocket"
            && method == GET
            && response.status == ACCEPTED_202

        when {
            isSse -> handleSse(context, response, body)
            isWebSocket -> handleWebSocket(context, resultContext, response, nettyRequest, channel)
            else -> writeResponse(context, response, HttpUtil.isKeepAlive(nettyRequest))
        }
    }

    @Suppress("UNCHECKED_CAST") // Body not cast to Publisher<HttpServerEvent> due to type erasure
    private fun handleSse(context: ChannelHandlerContext, response: HttpResponsePort, body: Any) {
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

            override fun onComplete() {
                context.close()
            }

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
        request: Context<HttpCall>,
        response: HttpResponsePort,
        nettyRequest: HttpRequest,
        channel: Channel
    ) {
        val session = NettyWsSession(context, HttpContext(request))
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

    private fun wsHandshake(nettyRequest: HttpRequest, channel: Channel) {
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
        val response = HttpResponse(body, status = INTERNAL_SERVER_ERROR_500)
        writeResponse(context, response, false)
    }

    private fun writeResponse(
        context: ChannelHandlerContext,
        hexagonResponse: HttpResponsePort,
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

    private fun nettyCookies(hexagonCookies: List<Cookie>) =
        hexagonCookies.map {
            DefaultCookie(it.name, it.value).apply {
                if (it.maxAge != -1L)
                    setMaxAge(it.maxAge)
                isSecure = it.secure
            }
        }

    internal fun nettyStatus(status: HttpStatus): HttpResponseStatus =
        when (status) {
            CONTINUE_100 -> CONTINUE
            SWITCHING_PROTOCOLS_101 -> SWITCHING_PROTOCOLS
            PROCESSING_102 -> PROCESSING

            OK_200 -> OK
            CREATED_201 -> CREATED
            ACCEPTED_202 -> ACCEPTED
            NON_AUTHORITATIVE_INFORMATION_203 -> NON_AUTHORITATIVE_INFORMATION
            NO_CONTENT_204 -> NO_CONTENT
            RESET_CONTENT_205 -> RESET_CONTENT
            PARTIAL_CONTENT_206 -> PARTIAL_CONTENT
            MULTI_STATUS_207 -> MULTI_STATUS

            MULTIPLE_CHOICES_300 -> MULTIPLE_CHOICES
            MOVED_PERMANENTLY_301 -> MOVED_PERMANENTLY
            FOUND_302 -> FOUND
            SEE_OTHER_303 -> SEE_OTHER
            NOT_MODIFIED_304 -> NOT_MODIFIED
            USE_PROXY_305 -> USE_PROXY
            TEMPORARY_REDIRECT_307 -> TEMPORARY_REDIRECT
            PERMANENT_REDIRECT_308 -> PERMANENT_REDIRECT

            BAD_REQUEST_400 -> BAD_REQUEST
            NOT_FOUND_404 -> NOT_FOUND
            UNAUTHORIZED_401 -> UNAUTHORIZED
            PAYMENT_REQUIRED_402 -> PAYMENT_REQUIRED
            FORBIDDEN_403 -> FORBIDDEN
            METHOD_NOT_ALLOWED_405 -> METHOD_NOT_ALLOWED
            NOT_ACCEPTABLE_406 -> NOT_ACCEPTABLE
            PROXY_AUTHENTICATION_REQUIRED_407 -> PROXY_AUTHENTICATION_REQUIRED
            REQUEST_TIMEOUT_408 -> REQUEST_TIMEOUT
            CONFLICT_409 -> CONFLICT
            GONE_410 -> GONE
            LENGTH_REQUIRED_411 -> LENGTH_REQUIRED
            PRECONDITION_FAILED_412 -> PRECONDITION_FAILED
            URI_TOO_LONG_414 -> REQUEST_URI_TOO_LONG
            UNSUPPORTED_MEDIA_TYPE_415 -> UNSUPPORTED_MEDIA_TYPE
            RANGE_NOT_SATISFIABLE_416 -> REQUESTED_RANGE_NOT_SATISFIABLE
            EXPECTATION_FAILED_417 -> EXPECTATION_FAILED
            MISDIRECTED_REQUEST_421 -> MISDIRECTED_REQUEST
            UNPROCESSABLE_CONTENT_422 -> UNPROCESSABLE_ENTITY
            LOCKED_423 -> LOCKED
            FAILED_DEPENDENCY_424 -> FAILED_DEPENDENCY
            UPGRADE_REQUIRED_426 -> UPGRADE_REQUIRED
            PRECONDITION_REQUIRED_428 -> PRECONDITION_REQUIRED
            TOO_MANY_REQUESTS_429 -> TOO_MANY_REQUESTS
            REQUEST_HEADER_FIELDS_TOO_LARGE_431 -> REQUEST_HEADER_FIELDS_TOO_LARGE

            INTERNAL_SERVER_ERROR_500 -> INTERNAL_SERVER_ERROR
            NOT_IMPLEMENTED_501 -> NOT_IMPLEMENTED
            BAD_GATEWAY_502 -> BAD_GATEWAY
            SERVICE_UNAVAILABLE_503 -> SERVICE_UNAVAILABLE
            GATEWAY_TIMEOUT_504 -> GATEWAY_TIMEOUT
            HTTP_VERSION_NOT_SUPPORTED_505 -> HTTP_VERSION_NOT_SUPPORTED
            VARIANT_ALSO_NEGOTIATES_506 -> VARIANT_ALSO_NEGOTIATES
            INSUFFICIENT_STORAGE_507 -> INSUFFICIENT_STORAGE
            NOT_EXTENDED_510 -> NOT_EXTENDED
            NETWORK_AUTHENTICATION_REQUIRED_511 -> NETWORK_AUTHENTICATION_REQUIRED

            else -> HttpResponseStatus(status.code, status.toString())
        }
}
