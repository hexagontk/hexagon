package com.hexagonkt.http.client.netty

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http.HttpUtil
import io.netty.handler.codec.http.LastHttpContent
import io.netty.util.CharsetUtil

class HttpClientResponseHandler : SimpleChannelInboundHandler<FullHttpResponse>() {

    lateinit var response: FullHttpResponse

    override fun channelRead0(ctx: ChannelHandlerContext, msg: FullHttpResponse) {
        System.err.println("STATUS: " + msg.status())
        System.err.println("VERSION: " + msg.protocolVersion())
        System.err.println()
        response = msg

        if (!msg.headers().isEmpty) {
            for (name in msg.headers().names()) {
                for (value in msg.headers().getAll(name)) {
                    System.err.println("HEADER: $name = $value")
                }
            }
            System.err.println()
        }

        if (HttpUtil.isTransferEncodingChunked(msg)) {
            System.err.println("CHUNKED CONTENT {")
        } else {
            System.err.println("CONTENT {")
        }

        System.err.print(msg.content().toString(CharsetUtil.UTF_8))
        System.err.flush()

        if (msg is LastHttpContent) {
            System.err.println("} END OF CONTENT")
            ctx.close()
        }
    }
}
