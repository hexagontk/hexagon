package com.hexagonkt.http.server.netty

import com.hexagonkt.http.handlers.coroutines.HttpContext
import com.hexagonkt.http.model.HttpRequestPort
import com.hexagonkt.http.model.ws.WsSession
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.websocketx.*
import java.net.URI

internal class NettyWsSession(
    nettyContext: ChannelHandlerContext,
    context: HttpContext,
) : WsSession {

    override val attributes: Map<*, *> by lazy { context.attributes }
    override val request: HttpRequestPort by lazy { context.request }
    override val exception: Exception? by lazy { context.exception }
    override val pathParameters: Map<String, String> by lazy { context.pathParameters }

    override val uri: URI get() = throw UnsupportedOperationException()

    private val channel = nettyContext.channel()

    override fun send(data: ByteArray) {
        val webSocketFrame = BinaryWebSocketFrame(Unpooled.wrappedBuffer(data))
        channel.writeAndFlush(webSocketFrame)
    }

    override fun send(text: String) {
        val webSocketFrame = TextWebSocketFrame(text)
        channel.writeAndFlush(webSocketFrame)
    }

    override fun ping(data: ByteArray) {
        channel.writeAndFlush(PingWebSocketFrame(Unpooled.wrappedBuffer(data)))
    }

    override fun pong(data: ByteArray) {
        channel.writeAndFlush(PongWebSocketFrame(Unpooled.wrappedBuffer(data)))
    }

    override fun close(status: Int, reason: String) {
        val webSocketFrame = CloseWebSocketFrame(status, reason)
        channel.writeAndFlush(webSocketFrame)
    }
}
