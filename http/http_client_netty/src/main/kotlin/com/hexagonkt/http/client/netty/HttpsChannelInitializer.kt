package com.hexagonkt.http.client.netty

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http2.DefaultHttp2Connection
import io.netty.handler.codec.http2.DelegatingDecompressorFrameListener
import io.netty.handler.codec.http2.Http2Connection
import io.netty.handler.codec.http2.HttpToHttp2ConnectionHandlerBuilder
import io.netty.handler.codec.http2.InboundHttp2ToHttpAdapterBuilder
import io.netty.handler.ssl.ApplicationProtocolNames
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler
import io.netty.handler.ssl.SslContext
import io.netty.handler.stream.ChunkedWriteHandler
import io.netty.util.concurrent.EventExecutorGroup

internal class HttpsChannelInitializer(
    private val sslContext: SslContext,
    private val executorGroup: EventExecutorGroup? = null,
    private val keepAliveHandler: Boolean = true,
    private val httpAggregatorHandler: Boolean = true,
    private val chunkedHandler: Boolean = true,
    private val maxContentLength: Int = Int.MAX_VALUE,
    val responseHandler: Http2ClientResponseHandler = Http2ClientResponseHandler(),
) : ChannelInitializer<SocketChannel>() {

    lateinit var settingsHandler: Http2SettingsHandler

    override fun initChannel(channel: SocketChannel) {
        settingsHandler = Http2SettingsHandler(channel.newPromise())

        val pipeline = channel.pipeline()
        val sslHandler = sslContext.newHandler(channel.alloc())

        pipeline.addLast(sslHandler)
        pipeline.addLast(HttpServerCodec())

        if (keepAliveHandler)
            pipeline.addLast(HttpServerKeepAliveHandler())
        if (httpAggregatorHandler)
            pipeline.addLast(HttpObjectAggregator(Int.MAX_VALUE))
        if (chunkedHandler)
            pipeline.addLast(ChunkedWriteHandler())

        val responseHandler = Http2ClientResponseHandler()
        val c = getClientAPNHandler(maxContentLength, settingsHandler, responseHandler)

        if (executorGroup == null)
            pipeline.addLast(c)
        else
            pipeline.addLast(executorGroup, c)
    }

    private fun getClientAPNHandler(
        maxContentLength: Int,
        settingsHandler: Http2SettingsHandler?,
        responseHandler: Http2ClientResponseHandler?
    ): ApplicationProtocolNegotiationHandler {
        val connection: Http2Connection = DefaultHttp2Connection(false)

        val connectionHandler = HttpToHttp2ConnectionHandlerBuilder()
            .frameListener(
                DelegatingDecompressorFrameListener(
                    connection,
                    InboundHttp2ToHttpAdapterBuilder(connection).maxContentLength(maxContentLength)
                        .propagateSettings(true)
                        .build()
                )
            )
            .connection(connection)
            .build()

        val clientAPNHandler: ApplicationProtocolNegotiationHandler =
            object : ApplicationProtocolNegotiationHandler(ApplicationProtocolNames.HTTP_2) {
                override fun configurePipeline(ctx: ChannelHandlerContext, protocol: String) {
                    if (ApplicationProtocolNames.HTTP_2 == protocol) {
                        val p = ctx.pipeline()
                        p.addLast(connectionHandler)
                        p.addLast(settingsHandler, responseHandler)
                        return
                    }
                    ctx.close()
                    throw IllegalStateException("Protocol: $protocol not supported")
                }
            }

        return clientAPNHandler
    }
}
