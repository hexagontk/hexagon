package com.hexagonkt.http.server.netty

import com.hexagonkt.http.SslSettings
import com.hexagonkt.http.handlers.HttpHandler
import com.hexagonkt.http.server.HttpServerSettings
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.ssl.SslContext
import io.netty.util.concurrent.EventExecutorGroup

internal class HttpsChannelInitializer(
    private val handlers: Map<HttpMethod, HttpHandler>,
    private val sslContext: SslContext,
    private val sslSettings: SslSettings,
    private val executorGroup: EventExecutorGroup?,
    private val settings: HttpServerSettings,
) : ChannelInitializer<SocketChannel>() {

    override fun initChannel(channel: SocketChannel) {
        val pipeline = channel.pipeline()
        val sslHandler = sslContext.newHandler(channel.alloc())
        val handlerSsl = if (sslSettings.clientAuth) sslHandler else null

        pipeline.addLast(sslHandler)
        pipeline.addLast(HttpServerCodec())
//        pipeline.addLast(HttpServerKeepAliveHandler())
        pipeline.addLast(HttpObjectAggregator(Int.MAX_VALUE))
//        pipeline.addLast(ChunkedWriteHandler())

        if (settings.zip)
            pipeline.addLast(HttpContentCompressor())

        val serverHandler = NettyServerHandler(handlers, handlerSsl)
        if (executorGroup == null)
            pipeline.addLast(serverHandler)
        else
            pipeline.addLast(executorGroup, serverHandler)
    }
}
