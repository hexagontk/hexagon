package com.hexagonkt.http.server.netty

import com.hexagonkt.http.bodyToBytes
import com.hexagonkt.http.model.*
import com.hexagonkt.http.server.handlers.PathHandler
import com.hexagonkt.http.server.model.HttpServerResponse
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener.CLOSE
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.HttpHeaderNames.*
import io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpResponseStatus.*
import io.netty.handler.codec.http.HttpVersion.HTTP_1_1
import io.netty.handler.codec.http.cookie.DefaultCookie
import io.netty.handler.codec.http.cookie.ServerCookieEncoder.STRICT
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory
import io.netty.handler.ssl.SslHandler
import io.netty.handler.ssl.SslHandshakeCompletionEvent
import java.lang.IllegalStateException
import java.net.InetSocketAddress
import java.security.cert.X509Certificate

internal class NettyServerHandler(
    private val handlers: Map<HttpMethod, PathHandler>,
    private val sslHandler: SslHandler?,
) : ChannelInboundHandlerAdapter() {

    private var certificates: List<X509Certificate> = emptyList()

    override fun channelRead(context: ChannelHandlerContext, nettyRequest: Any) {
        if (nettyRequest is FullHttpRequest) {
            val result = nettyRequest.decoderResult()

            if (result.isFailure)
                throw IllegalStateException(result.cause())

            val channel = context.channel()
            val address = channel.remoteAddress() as InetSocketAddress
            val method = nettyRequest.method()
            val headers = nettyRequest.headers()
            val request = NettyRequestAdapter(method, nettyRequest, certificates, address, headers)
            val response = handlers[method]?.process(request) ?: HttpServerResponse()

            val connection = headers[CONNECTION]?.lowercase()
            val upgrade = headers[UPGRADE]?.lowercase()

            if (connection == "upgrade" && upgrade == "websocket") {

                // Adding new handler to the existing pipeline to handle WebSocket Messages
                context.pipeline().replace(this, "webSocketHandler", NettyWebSocketHandler())

                // Do the Handshake to upgrade connection from HTTP to WebSocket protocol
                val host = headers["host"]
                val uri = nettyRequest.uri()
                val url = "ws://$host$uri"
                val wsFactory = WebSocketServerHandshakerFactory(url, null, true)
                val handShaker = wsFactory.newHandshaker(nettyRequest)

                if (handShaker == null)
                    WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(channel)
                else
                    handShaker.handshake(channel, nettyRequest)
            }
            else {
                writeResponse(context, response, HttpUtil.isKeepAlive(nettyRequest))
            }
        }
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
            hexagonHeaders.allValues.map { (k, v) -> headers.add(k, v) }

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
            SuccessStatus.ACCEPTED -> ACCEPTED
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
