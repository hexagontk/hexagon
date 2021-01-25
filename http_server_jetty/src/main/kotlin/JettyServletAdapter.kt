package com.hexagonkt.http.server.jetty

import com.hexagonkt.helpers.fail
import com.hexagonkt.http.Protocol
import com.hexagonkt.http.Protocol.HTTP
import com.hexagonkt.http.Protocol.HTTP2
import com.hexagonkt.http.Protocol.HTTPS
import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.ServerFeature
import com.hexagonkt.http.server.ServerFeature.ZIP as ZIP_FEATURE
import com.hexagonkt.http.server.ServerFeature.SESSIONS as SESSIONS_FEATURE
import com.hexagonkt.http.server.ServerPort
import com.hexagonkt.http.server.ServerSettings
import com.hexagonkt.http.server.servlet.ServletFilter
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory
import org.eclipse.jetty.server.*
import org.eclipse.jetty.server.handler.gzip.GzipHandler
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletContextHandler.NO_SESSIONS
import org.eclipse.jetty.servlet.ServletContextHandler.SESSIONS
import org.eclipse.jetty.util.ssl.SslContextFactory
import org.eclipse.jetty.util.thread.QueuedThreadPool
import java.security.KeyStore
import java.util.*
import javax.servlet.DispatcherType
import org.eclipse.jetty.server.Server as JettyServer

/**
 * Implements [ServerPort] using [JettyServer].
 */
class JettyServletAdapter : ServerPort {
    private var jettyServer: JettyServer? = null

    override fun runtimePort(): Int =
        ((jettyServer?.connectors?.get(0) ?: fail) as ServerConnector).localPort

    override fun started() = jettyServer?.isStarted ?: false

    override fun startup(server: Server) {
        val settings = server.settings
        val maxThreads = settings.options["maxThreads"] as? Int ?: 200
        val minThreads = settings.options["minThreads"] as? Int ?: 8
        val serverInstance = JettyServer(QueuedThreadPool(maxThreads, minThreads))
        jettyServer = serverInstance

        val context = createServerContext(settings)
        val filter = ServletFilter(server.contextRouter.flatRequestHandlers(), settings)
        val filterBind = context.servletContext.addFilter("filters", filter)
        val dispatcherTypes = EnumSet.allOf(DispatcherType::class.java)
        filterBind.addMappingForUrlPatterns(dispatcherTypes, true, "/*")

        val acceptors = settings.options["acceptors"] as? Int ?: -1
        val selectors = settings.options["selectors"] as? Int ?: -1
        val serverConnector =
            if (settings.sslSettings != null) setupSsl(settings, serverInstance)
            else ServerConnector(serverInstance, acceptors, selectors)

        serverConnector.host = settings.bindAddress.hostName
        serverConnector.port = settings.bindPort
        serverInstance.connectors = arrayOf(serverConnector)

        serverInstance.handler = context
        serverInstance.stopAtShutdown = true
        serverInstance.start()
    }

    override fun shutdown() {
        jettyServer?.stop()
    }

    override fun supportedProtocols(): Set<Protocol> =
        setOf(HTTP, HTTPS, HTTP2)

    override fun supportedFeatures(): Set<ServerFeature> =
        setOf(ZIP_FEATURE, SESSIONS_FEATURE)

    override fun supportedOptions(): Set<String> =
        setOf("maxThreads", "minThreads", "acceptors", "selectors")

    private fun createServerContext(settings: ServerSettings): ServletContextHandler {

        val features = settings.features
        val context =
            if (features.contains(ServerFeature.SESSIONS)) ServletContextHandler(SESSIONS)
            else ServletContextHandler(NO_SESSIONS)

        if (features.contains(ServerFeature.ZIP))
            context.insertHandler(GzipHandler())

        return context
    }

    private fun setupSsl(settings: ServerSettings, serverInstance: JettyServer): ServerConnector {
        val httpConfiguration = HttpConfiguration()
        httpConfiguration.secureScheme = "https"
        httpConfiguration.securePort = settings.bindPort
        httpConfiguration.addCustomizer(SecureRequestCustomizer())

        val sslContextFactory = SslContextFactory.Server()
        val sslSettings = settings.sslSettings ?: fail
        sslContextFactory.needClientAuth = sslSettings.clientAuth

        val keyStore = sslSettings.keyStore
        if (keyStore != null) {
            val keyStorePassword = sslSettings.keyStorePassword
            val keyStoreStream = keyStore.openStream()
            sslContextFactory.keyStore = KeyStore.getInstance("pkcs12")
            sslContextFactory.keyStore.load(keyStoreStream, keyStorePassword.toCharArray())
            sslContextFactory.setKeyStorePassword(keyStorePassword)
        }

        val trustStore = sslSettings.trustStore
        if (trustStore != null) {
            val trustStorePassword = sslSettings.trustStorePassword
            val trustStoreStream = trustStore.openStream()
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
