package com.hexagonkt.http.server.netty

import com.hexagonkt.core.fieldsMapOf
import com.hexagonkt.core.security.loadKeyStore
import com.hexagonkt.http.SslSettings
import com.hexagonkt.http.model.HttpProtocol
import com.hexagonkt.http.model.HttpProtocol.*
import com.hexagonkt.http.server.HttpServer
import com.hexagonkt.http.server.HttpServerFeature
import com.hexagonkt.http.server.HttpServerFeature.ASYNC
import com.hexagonkt.http.server.HttpServerFeature.ZIP
import com.hexagonkt.http.server.HttpServerPort
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.SslHandler
import java.net.InetSocketAddress
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.TrustManagerFactory

/**
 * Implements [HttpServerPort] using Netty [Channel].
 *
 * TODO Add HTTP/2 support
 */
class NettyAdapter(
    private val bossGroupThreads: Int = 1,
) : HttpServerPort {

    private var nettyChannel: Channel? = null
    private var bossEventLoop: NioEventLoopGroup? = null
    private var workerEventLoop: NioEventLoopGroup? = null

    override fun runtimePort(): Int =
        (nettyChannel?.localAddress() as? InetSocketAddress)?.port
            ?: error("Error fetching runtime port")

    override fun started() =
        nettyChannel?.isOpen ?: false

    override fun startUp(server: HttpServer) {
        val bossGroup = NioEventLoopGroup(1)
        val workerGroup = NioEventLoopGroup()

        try {
            val settings = server.settings
            val nettyServer = ServerBootstrap()

            nettyServer.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(channel: SocketChannel) {
                        val pipeline = channel.pipeline()

                        val sslSettings = settings.sslSettings
                        if (sslSettings != null) {
                            pipeline.addLast(createSslHandler(sslSettings, channel))
                        }

                        pipeline.addLast(HttpServerCodec())
                        pipeline.addLast(HttpServerExpectContinueHandler())
                        pipeline.addLast(NettyServerHandler(server))
                    }
                })

            val address = settings.bindAddress
            val port = settings.bindPort
            val future = nettyServer.bind(address, port).sync()

            nettyChannel = future.channel()
            bossEventLoop = bossGroup
            workerEventLoop = workerGroup
        }
        catch (e: Exception) {
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
        }
    }

    // TODO
    private fun createSslHandler(sslSettings: SslSettings, channel: SocketChannel): SslHandler? {
        val keyStoreUrl = sslSettings.keyStore ?: error("")
        val keyStorePassword = sslSettings.keyStorePassword
        val keyStore = loadKeyStore(keyStoreUrl, keyStorePassword)
        val keyManager = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        keyManager.init(keyStore, keyStorePassword.toCharArray())

        val sslContext = SslContextBuilder.forServer(keyManager)


        val trustStoreUrl = sslSettings.trustStore
        if (trustStoreUrl != null) {
            val trustStorePassword = sslSettings.trustStorePassword
            val trustStore = loadKeyStore(trustStoreUrl, trustStorePassword)
            val trustAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
            val trustManager = TrustManagerFactory.getInstance(trustAlgorithm)

            trustManager.init(trustStore)
            sslContext.trustManager(trustManager)
        }

        return sslContext.build().newHandler(channel.alloc())
    }

    override fun shutDown() {
        workerEventLoop?.shutdownGracefully()?.sync()
        bossEventLoop?.shutdownGracefully()?.sync()

        nettyChannel = null
        bossEventLoop = null
        workerEventLoop = null
    }

    override fun supportedProtocols(): Set<HttpProtocol> =
        setOf(HTTP, HTTPS, HTTP2)

    override fun supportedFeatures(): Set<HttpServerFeature> =
        setOf(ZIP, ASYNC)

    override fun options(): Map<String, *> =
        fieldsMapOf(
            NettyAdapter::bossGroupThreads to bossGroupThreads,
        )
}
