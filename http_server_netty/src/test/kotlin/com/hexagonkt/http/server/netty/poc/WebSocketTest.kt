package com.hexagonkt.http.server.netty.poc

import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.DefaultHttpHeaders
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http.HttpClientCodec
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.websocketx.*
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory.newHandshaker
import io.netty.handler.codec.http.websocketx.WebSocketVersion.V13
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory.INSTANCE
import io.netty.util.CharsetUtil
import org.junit.jupiter.api.Test
import java.net.URI
import kotlin.test.assertEquals

// TODO
internal class WebSocketTest {

    @Test fun `WS connections`() {
        main()

        val uri = URI("ws://localhost:${server.runtimePort}/ws")
        val handShaker = newHandshaker(uri, V13, null, true, DefaultHttpHeaders())
        val clientHandler = WebSocketClientHandler(handShaker)
        val bootstrap = client(clientHandler, uri)
        val channel = bootstrap.connect(uri.host, uri.port).sync().channel()

        clientHandler.handshakeSync()
        channel.writeAndFlush(TextWebSocketFrame("msg"))

        Thread.sleep(300)
        assertEquals("msg", clientHandler.result)

        channel.close().sync()
        bootstrap.config().group().shutdownGracefully()
        server.stop()
    }

    private fun client(clientHandler: WebSocketClientHandler, uri: URI): Bootstrap {
        val scheme = uri.scheme.lowercase()

        if (scheme !in setOf("ws", "wss"))
            error("Only WS(S) is supported")

        val channelInitializer = object : ChannelInitializer<SocketChannel>() {
            override fun initChannel(channel: SocketChannel) {
                val pipeline: ChannelPipeline = channel.pipeline()

                if (scheme == "wss") {
                    val sslCtx = SslContextBuilder.forClient().trustManager(INSTANCE).build()
                    pipeline.addLast(sslCtx.newHandler(channel.alloc(), uri.host, uri.port))
                }

                pipeline.addLast(
                    HttpClientCodec(),
                    HttpObjectAggregator(8192),
                    WebSocketClientCompressionHandler.INSTANCE,
                    clientHandler
                )
            }
        }

        return Bootstrap()
            .group(NioEventLoopGroup())
            .channel(NioSocketChannel::class.java)
            .handler(channelInitializer)
    }
}

private class WebSocketClientHandler(
    private val handShaker: WebSocketClientHandshaker,
) : ChannelInboundHandlerAdapter() {

    private lateinit var handshakeFuture: ChannelPromise
    var result: String? = null

    fun handshakeSync() {
        handshakeFuture.sync()
    }

    override fun handlerAdded(context: ChannelHandlerContext) {
        handshakeFuture = context.newPromise()
    }

    override fun channelActive(context: ChannelHandlerContext) {
        handShaker.handshake(context.channel())
    }

    override fun channelInactive(context: ChannelHandlerContext) {}

    override fun channelRead(context: ChannelHandlerContext, message: Any) {
        val channel = context.channel()

        if (!handShaker.isHandshakeComplete) {
            try {
                handShaker.finishHandshake(channel, message as FullHttpResponse)
                handshakeFuture.setSuccess()
            }
            catch (e: WebSocketHandshakeException) {
                handshakeFuture.setFailure(e)
            }
            return
        }

        when (message) {
            is FullHttpResponse -> {
                val status = message.status()
                val content = message.content().toString(CharsetUtil.UTF_8)
                error("Unexpected FullHttpResponse (getStatus=$status, content=$content)")
            }

            is TextWebSocketFrame -> {
                result = message.text()
            }

            is PongWebSocketFrame -> {}

            is CloseWebSocketFrame -> {
                channel.close()
            }
        }
    }
}
