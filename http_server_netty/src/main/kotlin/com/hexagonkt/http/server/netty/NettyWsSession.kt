package com.hexagonkt.http.server.netty

import com.hexagonkt.http.model.ws.WsCloseStatus
import com.hexagonkt.http.server.handlers.HttpServerContext
import com.hexagonkt.http.server.model.HttpServerRequestPort
import com.hexagonkt.http.server.model.ws.WsServerSession
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame

internal class NettyWsSession(
    nettyContext: ChannelHandlerContext,
    context: HttpServerContext,
) : WsServerSession {

    override val attributes: Map<*, *> by lazy { context.attributes }
    override val request: HttpServerRequestPort by lazy { context.request }
    override val exception: Exception? by lazy { context.exception }
    override val pathParameters: Map<String, String> by lazy { context.pathParameters }

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

    override fun close(status: WsCloseStatus, reason: String) {
        val webSocketFrame = CloseWebSocketFrame(status.code, reason)
        channel.writeAndFlush(webSocketFrame)
    }
}
