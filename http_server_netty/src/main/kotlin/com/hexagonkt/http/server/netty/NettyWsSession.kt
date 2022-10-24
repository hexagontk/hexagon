package com.hexagonkt.http.server.netty

import com.hexagonkt.http.model.HttpRequest
import com.hexagonkt.http.model.WsSession
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame

internal class NettyWsSession(
    private val context: ChannelHandlerContext,
    request: HttpRequest,
) : WsSession {

    override val httpRequest: HttpRequest = request

    override fun send(data: ByteArray) {
        val webSocketFrame = BinaryWebSocketFrame(Unpooled.wrappedBuffer(data))
        context.channel().writeAndFlush(webSocketFrame)
    }

    override fun send(text: String) {
        val webSocketFrame = TextWebSocketFrame(text)
        context.channel().writeAndFlush(webSocketFrame)
    }
}
