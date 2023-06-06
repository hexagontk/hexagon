package com.hexagonkt.http.server.netty.async

import com.hexagonkt.http.model.ws.WsSession
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.websocketx.*

internal class NettyWebSocketHandler(
    private val session: WsSession,
    private val onBinary: WsSession.(data: ByteArray) -> Unit = {},
    private val onText: WsSession.(text: String) -> Unit = {},
    private val onPing: WsSession.(data: ByteArray) -> Unit = {},
    private val onPong: WsSession.(data: ByteArray) -> Unit = {},
    private val onClose: WsSession.(status: Int, reason: String) -> Unit = { _, _ -> },
) : ChannelInboundHandlerAdapter() {

    override fun channelRead(context: ChannelHandlerContext, message: Any) {
        if (message !is WebSocketFrame)
            return

        val content = message.content()
        when (message) {
            is BinaryWebSocketFrame -> session.onBinary(content.retain().array())
            is TextWebSocketFrame -> session.onText(message.text())
            is PingWebSocketFrame -> session.onPing(content.retain().array())
            is PongWebSocketFrame -> session.onPong(content.retain().array())
            is CloseWebSocketFrame -> {
                // TODO Close the channel?
                session.onClose(message.statusCode(), message.reasonText())
            }
            else -> error("Unsupported WebSocketFrame")
        }
    }
}
