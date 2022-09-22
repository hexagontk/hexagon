package com.hexagonkt.http.server.netty

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.websocketx.*

internal class NettyWebSocketHandler : ChannelInboundHandlerAdapter() {

    override fun channelRead(context: ChannelHandlerContext, message: Any) {
        if (message is WebSocketFrame) {
            when (message) {
                is BinaryWebSocketFrame -> {
                    println("BinaryWebSocketFrame Received : " + message.content())
                }

                is TextWebSocketFrame -> {
                    val text = message.text()
                    val textWebSocketFrame = TextWebSocketFrame(text)
                    context.channel().writeAndFlush(textWebSocketFrame)
                    println("TextWebSocketFrame Received : $text")
                }

                is PingWebSocketFrame -> {
                    println("PingWebSocketFrame Received : " + message.content())
                }

                is PongWebSocketFrame -> {
                    println("PongWebSocketFrame Received : " + message.content())
                }

                is CloseWebSocketFrame -> {
                    println("CloseWebSocketFrame Received : ")
                    println("ReasonText :" + message.reasonText())
                    println("StatusCode : " + message.statusCode())
                }

                else -> {
                    println("Unsupported WebSocketFrame")
                }
            }
        }
    }
}
