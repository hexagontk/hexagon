package com.hexagontk.http.server.netty

import com.hexagontk.http.handlers.HttpHandler
import com.hexagontk.http.server.HttpServerSettings
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.stream.ChunkedWriteHandler
import java.util.concurrent.Executor

internal class HttpChannelInitializer(
    private val handlers: Map<HttpMethod, HttpHandler>,
    private val executor: Executor?,
    private val settings: HttpServerSettings,
    private val keepAliveHandler: Boolean = true,
    private val httpAggregatorHandler: Boolean = true,
    private val chunkedHandler: Boolean = true,
    private val enableWebsockets: Boolean = true,
) : ChannelInitializer<SocketChannel>() {

    override fun initChannel(channel: SocketChannel) {
        val pipeline = channel.pipeline()

        pipeline.addLast(HttpServerCodec())

        if (keepAliveHandler)
            pipeline.addLast(HttpServerKeepAliveHandler())
        if (httpAggregatorHandler)
            pipeline.addLast(HttpObjectAggregator(Int.MAX_VALUE))
        if (chunkedHandler)
            pipeline.addLast(ChunkedWriteHandler())
        if (settings.zip)
            pipeline.addLast(HttpContentCompressor())

        val nettyServerHandler = NettyServerHandler(handlers, executor, null, enableWebsockets)

        pipeline.addLast(nettyServerHandler)
    }
}
