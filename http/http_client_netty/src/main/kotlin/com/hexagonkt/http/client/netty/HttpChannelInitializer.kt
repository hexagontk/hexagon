package com.hexagonkt.http.client.netty

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.stream.ChunkedWriteHandler
import io.netty.util.concurrent.EventExecutorGroup

internal class HttpChannelInitializer(
    private val executorGroup: EventExecutorGroup?,
    private val keepAliveHandler: Boolean = true,
    private val httpAggregatorHandler: Boolean = true,
    private val chunkedHandler: Boolean = true,
) : ChannelInitializer<SocketChannel>() {

    override fun initChannel(channel: SocketChannel) {
        val pipeline = channel.pipeline()

        pipeline.addLast(HttpClientCodec())

        if (keepAliveHandler)
            pipeline.addLast(HttpServerKeepAliveHandler())
        if (httpAggregatorHandler)
            pipeline.addLast(HttpObjectAggregator(Int.MAX_VALUE))
        if (chunkedHandler)
            pipeline.addLast(ChunkedWriteHandler())

        val nettyServerHandler = HttpClientResponseHandler()

        if (executorGroup == null)
            pipeline.addLast(nettyServerHandler)
        else
            pipeline.addLast(executorGroup, nettyServerHandler)
    }
}
