package com.hexagonkt.http.client.netty

import com.hexagonkt.http.SslSettings
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.ssl.SslContext
import io.netty.handler.stream.ChunkedWriteHandler
import io.netty.util.concurrent.EventExecutorGroup

internal class HttpsChannelInitializer(
    private val sslContext: SslContext,
    private val sslSettings: SslSettings,
    private val executorGroup: EventExecutorGroup?,
    private val keepAliveHandler: Boolean = true,
    private val httpAggregatorHandler: Boolean = true,
    private val chunkedHandler: Boolean = true,
) : ChannelInitializer<SocketChannel>() {

    override fun initChannel(channel: SocketChannel) {
        val pipeline = channel.pipeline()
        val sslHandler = sslContext.newHandler(channel.alloc())
        val handlerSsl = if (sslSettings.clientAuth) sslHandler else null

        pipeline.addLast(sslHandler)
        pipeline.addLast(HttpServerCodec())

        if (keepAliveHandler)
            pipeline.addLast(HttpServerKeepAliveHandler())
        if (httpAggregatorHandler)
            pipeline.addLast(HttpObjectAggregator(Int.MAX_VALUE))
        if (chunkedHandler)
            pipeline.addLast(ChunkedWriteHandler())

        val serverHandler = Http2ClientResponseHandler()

        if (executorGroup == null)
            pipeline.addLast(serverHandler)
        else
            pipeline.addLast(executorGroup, serverHandler)
    }
}
