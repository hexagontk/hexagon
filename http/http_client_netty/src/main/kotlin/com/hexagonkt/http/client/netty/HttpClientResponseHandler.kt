package com.hexagonkt.http.client.netty

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.FullHttpResponse
import java.util.concurrent.CompletableFuture

class HttpClientResponseHandler : SimpleChannelInboundHandler<FullHttpResponse>() {

    var response: CompletableFuture<FullHttpResponse> = CompletableFuture()

    override fun channelRead0(ctx: ChannelHandlerContext, msg: FullHttpResponse) {
        response.complete(msg)
        ctx.close()
    }
}
