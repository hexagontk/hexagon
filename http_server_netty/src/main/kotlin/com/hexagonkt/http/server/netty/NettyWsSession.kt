package com.hexagonkt.http.server.netty

import com.hexagonkt.http.model.HttpRequest
import com.hexagonkt.http.model.ws.WsCloseStatus
import com.hexagonkt.http.server.model.WsServerSession
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame

internal class NettyWsSession(
    context: ChannelHandlerContext,
    request: HttpRequest,
) : WsServerSession {

    override val httpRequest: HttpRequest = request
    private val channel = context.channel()

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
