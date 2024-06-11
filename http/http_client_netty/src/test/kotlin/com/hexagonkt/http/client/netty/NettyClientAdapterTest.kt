package com.hexagonkt.http.client.netty

import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.model.HttpRequest
import com.hexagonkt.http.server.HttpServerSettings
import com.hexagonkt.http.server.jetty.serve
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelInitializer
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.*
import io.netty.util.CharsetUtil
import org.junit.jupiter.api.Test
import java.net.URI
import java.util.concurrent.CompletableFuture
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class NettyClientAdapterTest {

    @Test fun `Send request without starting client`() {
        val client = HttpClient(NettyClientAdapter())
        val request = HttpRequest()
        val message = assertFailsWith<IllegalStateException> { client.send(request) }.message
        val expectedMessage = "HTTP client *MUST BE STARTED* before sending requests"
        assertEquals(expectedMessage, message)
    }

    fun sendHttpRequest(uri: URI): String? {

        serve(HttpServerSettings(bindPort = 2001)) {
            get {
                ok("foo")
            }
        }

        val group = NioEventLoopGroup()
        val responseContent = CompletableFuture<String>()

        try {
            val bootstrap = Bootstrap()
            bootstrap
                .group(group)
                .channel(NioSocketChannel::class.java)
                .handler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        ch.pipeline().addLast(HttpClientCodec())
                        ch.pipeline().addLast(HttpObjectAggregator(8192))
                        ch.pipeline().addLast(
                            object : SimpleChannelInboundHandler<FullHttpResponse>() {
                                override fun channelRead0(
                                    ctx: io.netty.channel.ChannelHandlerContext,
                                    msg: FullHttpResponse
                                ) {
                                    responseContent.complete(
                                        msg.content().toString(CharsetUtil.UTF_8)
                                    )
                                    ctx.close()
                                }

                                override fun exceptionCaught(
                                    ctx: io.netty.channel.ChannelHandlerContext,
                                    cause: Throwable
                                ) {
                                    responseContent.completeExceptionally(cause)
                                    ctx.close()
                                }
                            }
                        )
                    }
                })

            // Make the connection attempt
            val channel =
                bootstrap.connect(uri.host, if (uri.port == -1) 80 else uri.port).sync().channel()

            // Prepare the HTTP request
            val request = DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, uri.rawPath, Unpooled.EMPTY_BUFFER
            )

            // Set HTTP headers
            request.headers().set(HttpHeaderNames.HOST, uri.host)

            // Send the HTTP request
            channel.writeAndFlush(request).sync()

            // Close the channel gracefully
            channel.closeFuture().sync()

            // Wait for the response
            responseContent.join()
        } finally {
            // Shut down the event loop to terminate all threads
            group.shutdownGracefully()
        }

        return responseContent.get()
    }

    @Test fun test() {
        try {
            val uri = URI("http://localhost:2001")
            val response = sendHttpRequest(uri)
            println("Response received: $response")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
