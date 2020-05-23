package com.hexagonkt.http.server.jetty

import com.hexagonkt.helpers.error
import com.hexagonkt.helpers.stream
import com.hexagonkt.http.Protocol.HTTP2
import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.ServerPort
import com.hexagonkt.http.server.ServerSettings
import com.hexagonkt.http.server.servlet.ServletFilter
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory
import org.eclipse.jetty.server.*
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletContextHandler.SESSIONS
import org.eclipse.jetty.util.component.AbstractLifeCycle.AbstractLifeCycleListener
import org.eclipse.jetty.util.component.LifeCycle
import org.eclipse.jetty.util.ssl.SslContextFactory
import java.net.InetSocketAddress
import java.security.KeyStore
import java.util.*
import javax.servlet.DispatcherType
import org.eclipse.jetty.server.Server as JettyServer

class JettyServletAdapter : ServerPort {
    private var jettyServer: JettyServer? = null

    override fun runtimePort(): Int =
        ((jettyServer?.connectors?.get(0) ?: error) as ServerConnector).localPort

    override fun started() = jettyServer?.isStarted ?: false

    override fun startup(server: Server) {
        val settings = server.settings
        val serverInstance = JettyServer(InetSocketAddress(settings.bindAddress, settings.bindPort))
        jettyServer = serverInstance

        val context = ServletContextHandler(SESSIONS)
        context.addLifeCycleListener(object : AbstractLifeCycleListener() {
            override fun lifeCycleStarting(event: LifeCycle?) {
                val filter = ServletFilter(server.contextRouter.flatRequestHandlers())
                val dispatcherTypes = EnumSet.allOf(DispatcherType::class.java)
                val filterBind = context.servletContext.addFilter("filters", filter)
                filterBind.addMappingForUrlPatterns(dispatcherTypes, true, "/*")
            }
        })

        if (settings.sslSettings != null) {
            val serverConnector = setupSsl(settings, serverInstance)
            serverConnector.port = settings.bindPort
            serverInstance.connectors = arrayOf(serverConnector)
        }

        serverInstance.handler = context
        serverInstance.stopAtShutdown = true
        serverInstance.start()
    }

    override fun shutdown() {
        jettyServer?.stop()
    }

    private fun setupSsl(settings: ServerSettings, serverInstance: JettyServer): ServerConnector {
        val httpConfiguration = HttpConfiguration()
        httpConfiguration.secureScheme = "https"
        httpConfiguration.securePort = settings.bindPort
        httpConfiguration.addCustomizer(SecureRequestCustomizer())

        val sslContextFactory = SslContextFactory.Server()
        val sslSettings = settings.sslSettings ?: error
        sslContextFactory.needClientAuth = sslSettings.clientAuth

        val keyStore = sslSettings.keyStore
        if (keyStore != null) {
            val keyStorePassword = sslSettings.keyStorePassword
            val keyStoreStream = keyStore.stream()
            sslContextFactory.keyStore = KeyStore.getInstance("pkcs12")
            sslContextFactory.keyStore.load(keyStoreStream, keyStorePassword.toCharArray())
            sslContextFactory.setKeyStorePassword(keyStorePassword)
        }

        val trustStore = sslSettings.trustStore
        if (trustStore != null) {
            val trustStorePassword = sslSettings.trustStorePassword
            val trustStoreStream = trustStore.stream()
            sslContextFactory.trustStore = KeyStore.getInstance("pkcs12")
            sslContextFactory.trustStore.load(trustStoreStream, trustStorePassword.toCharArray())
            sslContextFactory.setTrustStorePassword(trustStorePassword)
        }

        return if (settings.protocol != HTTP2)
            ServerConnector(
                serverInstance,
                SslConnectionFactory(sslContextFactory, "http/1.1"),
                HttpConnectionFactory(httpConfiguration)
            )
        else {
            val alpn = ALPNServerConnectionFactory()
            ServerConnector(
                serverInstance,
                SslConnectionFactory(sslContextFactory, alpn.protocol),
                alpn,
                HTTP2ServerConnectionFactory(httpConfiguration),
                HttpConnectionFactory(httpConfiguration)
            )
        }
    }
}
