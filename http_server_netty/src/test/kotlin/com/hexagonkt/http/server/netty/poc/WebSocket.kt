package com.hexagonkt.http.server.netty.poc

import com.hexagonkt.core.allInterfaces
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.channel.ChannelFutureListener.CLOSE
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.HttpHeaderNames.CONNECTION
import io.netty.handler.codec.http.HttpHeaderNames.UPGRADE
import io.netty.handler.codec.http.HttpResponseStatus.OK
import io.netty.handler.codec.http.HttpVersion.HTTP_1_1
import io.netty.handler.codec.http.websocketx.*
import io.netty.handler.stream.ChunkedWriteHandler
import io.netty.util.concurrent.DefaultEventExecutorGroup
import io.netty.util.concurrent.EventExecutorGroup
import kotlin.Int.Companion.MAX_VALUE

private const val bossGroupThreads = 1
private const val workerGroupThreads = 1
private const val executorThreads = 1

private const val soBacklog: Int = 4 * 1_024
private const val soReuseAddr: Boolean = true
private const val soKeepAlive: Boolean = true

private val bindAddress = allInterfaces
private const val bindPort = 8080

/*
 * Check: https://medium.com/@irunika/how-to-write-a-http-websocket-server-using-netty-f3c136adcba9
 */
fun main() {
    val bossGroup = groupSupplier(bossGroupThreads)
    val workerGroup = groupSupplier(workerGroupThreads)
    val executorGroup =
        if (executorThreads > 0) DefaultEventExecutorGroup(executorThreads)
        else null

    try {
        val initializer = createInitializer(executorGroup)
        val nettyServer = serverBootstrapSupplier(bossGroup, workerGroup).childHandler(initializer)
        nettyServer.bind(bindAddress, bindPort).sync()
    }
    catch (e: Exception) {
        bossGroup.shutdownGracefully()
        workerGroup.shutdownGracefully()
        executorGroup?.shutdownGracefully()
    }
}

private fun groupSupplier(it: Int): MultithreadEventLoopGroup =
    NioEventLoopGroup(it)

private fun createInitializer(group: DefaultEventExecutorGroup?) =
    HttpChannelInitializer(group)

private fun serverBootstrapSupplier(
    bossGroup: MultithreadEventLoopGroup,
    workerGroup: MultithreadEventLoopGroup,
): ServerBootstrap =
    ServerBootstrap().group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel::class.java)
        .option(ChannelOption.SO_BACKLOG, soBacklog)
        .option(ChannelOption.SO_REUSEADDR, soReuseAddr)
        .childOption(ChannelOption.SO_KEEPALIVE, soKeepAlive)
        .childOption(ChannelOption.SO_REUSEADDR, soReuseAddr)

private class HttpChannelInitializer(
    private val executorGroup: EventExecutorGroup?,
) : ChannelInitializer<SocketChannel>() {

    override fun initChannel(channel: SocketChannel) {
        val pipeline = channel.pipeline()

        pipeline.addLast(HttpServerCodec())
        pipeline.addLast(HttpServerKeepAliveHandler())
        pipeline.addLast(HttpObjectAggregator(MAX_VALUE))
        pipeline.addLast(ChunkedWriteHandler())

        if (executorGroup == null)
            pipeline.addLast(NettyServerHandler())
        else
            pipeline.addLast(executorGroup, NettyServerHandler())
    }
}

private class NettyServerHandler : ChannelInboundHandlerAdapter() {

    override fun channelRead(context: ChannelHandlerContext, nettyRequest: Any) {
        if (nettyRequest is HttpRequest) {
            val headers = nettyRequest.headers()
            val connection = headers[CONNECTION].lowercase()
            val upgrade = headers[UPGRADE].lowercase()

            if (connection == "upgrade" && upgrade == "websocket") {

                //Adding new handler to the existing pipeline to handle WebSocket Messages
                context.pipeline().replace(this, "webSocketHandler", WebSocketHandler())
                println("WebSocketHandler added to the pipeline")
                println("Opened Channel : " + context.channel())

                //Do the Handshake to upgrade connection from HTTP to WebSocket protocol
                println("Handshaking....")
                handleHandshake(context, nettyRequest)
                println("Handshake is done")
            }
            else {
                val buffer = Unpooled.copiedBuffer("OK".toByteArray())
                val response = DefaultFullHttpResponse(HTTP_1_1, OK, buffer)

                context.writeAndFlush(response).addListener(CLOSE)
            }
        }
        else {
            println("Incoming request is unknown")
        }
    }

    private var handShaker: WebSocketServerHandshaker? = null

    private fun handleHandshake(ctx: ChannelHandlerContext, req: HttpRequest) {
        val wsFactory = WebSocketServerHandshakerFactory(getWebSocketURL(req), null, true)

        handShaker = wsFactory.newHandshaker(req)

        if (handShaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel())
        }
        else {
            handShaker?.handshake(ctx.channel(), req)
        }
    }

    private fun getWebSocketURL(req: HttpRequest): String {
        println("Req URI : " + req.uri())
        val url = "ws://" + req.headers()["Host"] + req.uri()
        println("Constructed URL : $url")
        return url
    }
}

private class WebSocketHandler : ChannelInboundHandlerAdapter() {

    override fun channelRead(context: ChannelHandlerContext, message: Any) {
        if (message is WebSocketFrame) {
            println("This is a WebSocket frame")
            println("Client Channel : " + context.channel())

            when (message) {
                is BinaryWebSocketFrame -> {
                    println("BinaryWebSocketFrame Received : " + message.content())
                }

                is TextWebSocketFrame -> {
                    val text = message.text()
                    val textWebSocketFrame = TextWebSocketFrame(text)
                    context.channel().writeAndFlush(textWebSocketFrame)
                    println("TextWebSocketFrame Received : $text")
                }

                is PingWebSocketFrame -> {
                    println("PingWebSocketFrame Received : " + message.content())
                }

                is PongWebSocketFrame -> {
                    println("PongWebSocketFrame Received : " + message.content())
                }

                is CloseWebSocketFrame -> {
                    println("CloseWebSocketFrame Received : ")
                    println("ReasonText :" + message.reasonText())
                    println("StatusCode : " + message.statusCode())
                }

                else -> {
                    println("Unsupported WebSocketFrame")
                }
            }
        }
    }
}
