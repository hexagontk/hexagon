package com.hexagontk.http.server.netty

import com.hexagontk.core.toText
import com.hexagontk.handlers.Context
import com.hexagontk.http.handlers.bodyToBytes
import com.hexagontk.http.model.*
import com.hexagontk.http.model.Cookie
import com.hexagontk.http.handlers.HttpHandler
import com.hexagontk.http.handlers.HttpContext
import com.hexagontk.http.model.CookieSameSite.*
import com.hexagontk.http.model.HttpCall
import com.hexagontk.http.model.HttpResponse
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
import io.netty.handler.codec.http.cookie.CookieHeaderNames.SameSite.*
import io.netty.handler.codec.http.cookie.DefaultCookie
import io.netty.handler.codec.http.cookie.ServerCookieEncoder.STRICT as STRICT_ENCODER
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory
import io.netty.handler.ssl.SslHandler
import io.netty.handler.ssl.SslHandshakeCompletionEvent
import java.security.cert.X509Certificate
import java.util.concurrent.Executor
import java.util.concurrent.Flow.*
import com.hexagontk.http.model.HttpRequest as HexagonHttpRequest

internal class NettyServerHandler(
    private val handlers: Map<HttpMethod, HttpHandler>,
    executor: Executor?,
    private val sslHandler: SslHandler?,
    private val enableWebsockets: Boolean = true,
) : ChannelInboundHandlerAdapter() {

    private var certificates: List<X509Certificate> = emptyList()
    private val httpRequestProcessor: (ChannelHandlerContext, HttpRequest) -> Unit =
        executor
            ?.let { x ->
                { context: ChannelHandlerContext, nettyRequest: HttpRequest ->
                    x.execute {
                        try {
                            readHttpRequest(context, nettyRequest)
                        }
                        catch (e: Exception) {
                            exceptionCaught(context, e)
                        }
                    }
                }
            }
            ?: this::readHttpRequest

    override fun channelRead(context: ChannelHandlerContext, nettyRequest: Any) {
        if (nettyRequest is HttpRequest)
            httpRequestProcessor(context, nettyRequest)
    }

    private fun readHttpRequest(context: ChannelHandlerContext, nettyRequest: HttpRequest) {
        val result = nettyRequest.decoderResult()

        if (result.isFailure)
            throw IllegalStateException(result.cause())

        val channel = context.channel()
        val method = nettyRequest.method()
        val pathHandler = handlers[method]

        val headers = nettyRequest.headers()
        val request = NettyRequestAdapter(method, nettyRequest, certificates, channel, headers)

        if (pathHandler == null) {
            writeResponse(context, request, HttpResponse(), HttpUtil.isKeepAlive(nettyRequest))
            return
        }

        val resultContext = pathHandler.process(request)
        val response = resultContext.event.response

        val isWebSocket =
            if (enableWebsockets) isWebsocket(headers, method, response.status)
            else false

        val body = response.body
        val isSse = body is Publisher<*>

        when {
            isSse -> handleSse(context, request, response, body)
            isWebSocket -> handleWebSocket(context, resultContext, response, nettyRequest, channel)
            else -> writeResponse(context, request, response, HttpUtil.isKeepAlive(nettyRequest))
        }
    }

    private fun isWebsocket(headers: HttpHeaders, method: HttpMethod, status: Int): Boolean {
        val connection = headers[CONNECTION]?.lowercase()
        val upgrade = headers[UPGRADE]?.lowercase()
        return connection == "upgrade"
            && upgrade == "websocket"
            && method == GET
            && status == ACCEPTED_202
    }

    @Suppress("UNCHECKED_CAST") // Body not cast to Publisher<HttpServerEvent> due to type erasure
    private fun handleSse(
        context: ChannelHandlerContext,
        hexagonRequest: HttpRequestPort,
        response: HttpResponsePort, body: Any,
    ) {
        val status = nettyStatus(response.status)
        val nettyResponse = DefaultHttpResponse(HTTP_1_1, status)
        val headers = nettyResponse.headers()

        val hexagonHeaders = response.headers
        if (hexagonHeaders.fields.isNotEmpty())
            hexagonHeaders.all.map { (k, v) -> headers.add(k, v.map { it.text }) }

        val hexagonCookies = response.cookies
        if (hexagonCookies.isNotEmpty()) {
            val cookies = nettyCookies(hexagonRequest.protocol.secure, hexagonCookies)
            headers[SET_COOKIE] = STRICT_ENCODER.encode(cookies)
        }

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
        val response = HttpResponse(bodyToBytes(cause.toText()), status = INTERNAL_SERVER_ERROR_500)
        writeResponse(context, HexagonHttpRequest(), response, false)
    }

    private fun writeResponse(
        context: ChannelHandlerContext,
        hexagonRequest: HttpRequestPort,
        hexagonResponse: HttpResponsePort,
        keepAlive: Boolean,
    ) {
        val buffer = Unpooled.copiedBuffer(bodyToBytes(hexagonResponse.body))
        val status = nettyStatus(hexagonResponse.status)
        val response = DefaultFullHttpResponse(HTTP_1_1, status, buffer)

        val headers = response.headers()
        val hexagonHeaders = hexagonResponse.headers
        if (hexagonHeaders.fields.isNotEmpty())
            hexagonHeaders.all.map { (k, v) -> headers.add(k, v.map { it.text }) }

        val hexagonCookies = hexagonResponse.cookies
        if (hexagonCookies.isNotEmpty()) {
            val cookies = nettyCookies(hexagonRequest.protocol.secure, hexagonCookies)
            headers[SET_COOKIE] = STRICT_ENCODER.encode(cookies)
        }

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

    private fun nettyCookies(secureRequest: Boolean, hexagonCookies: List<Cookie>) =
        hexagonCookies
            .filter { if (secureRequest) true else !it.secure }
            .map {
                DefaultCookie(it.name, it.value).apply {
                    if (it.maxAge != -1L)
                        setMaxAge(it.maxAge)
                    isSecure = it.secure
                    setPath(it.path)
                    setDomain(it.domain)
                    isHttpOnly = it.httpOnly
                    it.domain?.let(::setDomain)
                    it.sameSite
                        ?.let { ss ->
                            when (ss) {
                                STRICT -> Strict
                                LAX -> Lax
                                NONE -> None
                            }
                        }
                        ?.let(::setSameSite)
                }
            }

    internal fun nettyStatus(status: Int): HttpResponseStatus =
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

            else -> HttpResponseStatus(status, status.toString())
        }
}
