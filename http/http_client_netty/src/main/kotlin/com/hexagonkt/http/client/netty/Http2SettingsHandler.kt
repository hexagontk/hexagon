package com.hexagonkt.http.client.netty

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http2.Http2Settings
import java.util.concurrent.TimeUnit

internal class Http2SettingsHandler(private val promise: ChannelPromise) :
    SimpleChannelInboundHandler<Http2Settings>() {
    fun awaitSettings(timeout: Long, unit: TimeUnit?) {
        check(promise.awaitUninterruptibly(timeout, unit)) { "Timed out waiting for settings" }
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: Http2Settings) {
        promise.setSuccess()

        ctx.pipeline().remove(this)
    }
}
