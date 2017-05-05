package co.there4.hexagon.server.backend.servlet

import co.there4.hexagon.util.err
import co.there4.hexagon.server.Server
import co.there4.hexagon.server.backend.ServerEngine
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletContextHandler.SESSIONS
import org.eclipse.jetty.util.component.LifeCycle
import java.net.InetSocketAddress
import java.util.*
import javax.servlet.DispatcherType
import org.eclipse.jetty.server.Server as JettyServer
import java.net.InetAddress.getByName as address

/**
 * TODO .
 */
class JettyServletEngine : ServerEngine {

    private var jettyServer: JettyServer? = null

    override fun runtimePort(): Int =
        ((jettyServer?.connectors?.get(0) ?: err) as ServerConnector).localPort.let {
            if (it == -1) error("Jetty port uninitialized. Use lazy evaluation for HTTP client ;)")
            else it
        }

    override fun started() = jettyServer?.isStarted ?: false

    override fun startup(server: Server, settings: Map<String, *>) {
        jettyServer = JettyServer(InetSocketAddress(server.bindAddress, server.bindPort))

        val context = ServletContextHandler(SESSIONS)
        context.addLifeCycleListener(object : LifeCycle.Listener {
            override fun lifeCycleStopped(event: LifeCycle?) { /* Do nothing */ }
            override fun lifeCycleStopping(event: LifeCycle?) { /* Do nothing */ }
            override fun lifeCycleStarted(event: LifeCycle?) { /* Do nothing */ }
            override fun lifeCycleFailure(event: LifeCycle?, cause: Throwable?) { /* Do nothing */ }

            override fun lifeCycleStarting(event: LifeCycle?) {
                val filter = ServletFilter (server.router)
                val dispatcherTypes = EnumSet.allOf(DispatcherType::class.java)
                val filterBind = context.servletContext.addFilter("filters", filter)
                filterBind.setAsyncSupported(false)
                filterBind.addMappingForUrlPatterns(dispatcherTypes, true, "/*")
            }
        })

        jettyServer?.handler = context
        jettyServer?.start()
    }

    override fun shutdown() {
        jettyServer?.stopAtShutdown = true
        jettyServer?.stop()
    }
}
