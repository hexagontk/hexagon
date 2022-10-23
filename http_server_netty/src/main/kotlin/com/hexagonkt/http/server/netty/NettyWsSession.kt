package com.hexagonkt.http.server.netty

import com.hexagonkt.http.server.model.HttpServerRequestPort
import com.hexagonkt.http.server.model.WsSession
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame

internal class NettyWsSession(
    private val context: ChannelHandlerContext,
    request: NettyRequestAdapter,
) : WsSession {

    override val httpRequest: HttpServerRequestPort = request

    override fun send(data: ByteArray) {
        val webSocketFrame = BinaryWebSocketFrame(Unpooled.wrappedBuffer(data))
        context.channel().writeAndFlush(webSocketFrame)
    }

    override fun send(text: String) {
        val webSocketFrame = TextWebSocketFrame(text)
        context.channel().writeAndFlush(webSocketFrame)
    }
}
