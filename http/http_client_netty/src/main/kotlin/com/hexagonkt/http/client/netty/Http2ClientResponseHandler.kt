package com.hexagonkt.http.client.netty

import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http2.HttpConversionUtil
import io.netty.util.CharsetUtil
import java.util.concurrent.TimeUnit

class Http2ClientResponseHandler : SimpleChannelInboundHandler<FullHttpResponse>() {
    private val streamIdMap: MutableMap<Int, MapValues> = HashMap()

    fun put(streamId: Int, writeFuture: ChannelFuture, promise: ChannelPromise): MapValues? {
        return streamIdMap.put(streamId, MapValues(writeFuture, promise))
    }

    fun awaitResponses(timeout: Long, unit: TimeUnit): String? {
        val itr: MutableIterator<Map.Entry<Int, MapValues>> = streamIdMap.entries.iterator()

        var response: String? = null

        while (itr.hasNext()) {
            val entry = itr.next()
            val writeFuture = entry.value.writeFuture

            check(
                writeFuture.awaitUninterruptibly(
                    timeout,
                    unit
                )
            ) { "Timed out waiting to write for stream id " + entry.key }
            if (!writeFuture.isSuccess) {
                throw RuntimeException(writeFuture.cause())
            }
            val promise = entry.value.promise

            check(promise.awaitUninterruptibly(timeout, unit)) {
                "Timed out waiting for response on stream id " + entry.key
            }
            if (!promise.isSuccess) {
                throw RuntimeException(promise.cause())
            }
            response = entry.value.response

            itr.remove()
        }

        return response
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: FullHttpResponse) {
        val streamId = msg.headers().getInt(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text()) ?: return

        val value = streamIdMap[streamId]

        if (value == null) {
            ctx.close()
        } else {
            val content = msg.content()
            if (content.isReadable) {
                val contentLength = content.readableBytes()
                val arr = ByteArray(contentLength)
                content.readBytes(arr)
                val response = String(arr, 0, contentLength, CharsetUtil.UTF_8)
                value.response = response
            }

            value.promise
                .setSuccess()
        }
    }

    class MapValues(var writeFuture: ChannelFuture, var promise: ChannelPromise) {
        var response: String? = null
    }
}
