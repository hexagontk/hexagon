package com.hexagonkt.http.server.netty

import com.hexagonkt.http.handlers.HttpHandler
import com.hexagonkt.http.server.HttpServerSettings
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.stream.ChunkedWriteHandler
import io.netty.util.concurrent.EventExecutorGroup

internal class HttpChannelInitializer(
    private val handlers: Map<HttpMethod, HttpHandler>,
    private val executorGroup: EventExecutorGroup?,
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

        val nettyServerHandler = NettyServerHandler(handlers, null, enableWebsockets)

        if (executorGroup == null)
            pipeline.addLast(nettyServerHandler)
        else
            pipeline.addLast(executorGroup, nettyServerHandler)
    }
}
