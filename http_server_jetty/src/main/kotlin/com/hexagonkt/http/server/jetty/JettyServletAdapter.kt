package com.hexagonkt.http.server.jetty

import com.hexagonkt.helpers.Resource
import com.hexagonkt.helpers.error
import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.ServerPort
import com.hexagonkt.http.server.servlet.ServletFilter
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

/**
 * TODO .
 */
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
            val httpConfiguration = HttpConfiguration()
            httpConfiguration.secureScheme = "https"
            httpConfiguration.securePort = settings.bindPort
            httpConfiguration.addCustomizer(SecureRequestCustomizer())

            val sslContextFactory = SslContextFactory.Server()
            val keyStorePassword = settings.sslSettings?.keyStorePassword ?: error()
            val trustStorePassword = settings.sslSettings?.trustStorePassword ?: error()
            sslContextFactory.keyStore = KeyStore.getInstance("pkcs12")
            sslContextFactory.keyStore.load(Resource("ssl/server.p12").requireStream(), keyStorePassword.toCharArray())
            sslContextFactory.setKeyStorePassword(keyStorePassword)
            sslContextFactory.trustStore = KeyStore.getInstance("pkcs12")
            sslContextFactory.trustStore.load(Resource("ssl/trust_store.p12").requireStream(), trustStorePassword.toCharArray())
            sslContextFactory.setTrustStorePassword(trustStorePassword)

            val serverConnector = ServerConnector(
                serverInstance,
                SslConnectionFactory(sslContextFactory, "http/1.1"),
                HttpConnectionFactory(httpConfiguration)
            )

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
}
