package com.hexagonkt.http.server.jetty

import com.hexagonkt.helpers.error
import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.ServerPort
import com.hexagonkt.http.server.servlet.ServletFilter
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletContextHandler.SESSIONS
import org.eclipse.jetty.util.component.AbstractLifeCycle.AbstractLifeCycleListener
import org.eclipse.jetty.util.component.LifeCycle
import java.net.InetSocketAddress
import java.util.*
import javax.servlet.DispatcherType
import org.eclipse.jetty.server.Server as JettyServer

/**
 * TODO .
 */
class JettyServletAdapter(private val async: Boolean = false) : ServerPort {
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
                filterBind.setAsyncSupported(async)
                filterBind.addMappingForUrlPatterns(dispatcherTypes, true, "/*")
            }
        })

        serverInstance.handler = context
        serverInstance.stopAtShutdown = true
        serverInstance.start()
    }

    override fun shutdown() {
        jettyServer?.stop()
    }
}
