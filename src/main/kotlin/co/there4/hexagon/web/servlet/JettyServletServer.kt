package co.there4.hexagon.web.servlet

import co.there4.hexagon.web.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.server.Server as JettyServer
import org.eclipse.jetty.server.session.HashSessionIdManager
import org.eclipse.jetty.server.session.HashSessionManager
import org.eclipse.jetty.server.session.SessionHandler
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.util.component.LifeCycle

import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.*
import javax.servlet.DispatcherType
import java.net.InetAddress.getByName as address

/**
 * @author jam
 */
class JettyServletServer(bindAddress: InetAddress = address ("localhost"), bindPort: Int = 2010):
    Server(bindAddress, bindPort) {

    private val jettyServer = JettyServer(InetSocketAddress(bindAddress, bindPort))

    override val runtimePort: Int
        get() = (jettyServer.connectors[0] as ServerConnector).localPort.let {
            if (it == -1) error("Jetty port uninitialized. Use lazy evaluation for HTTP client ;)")
            else it
        }

    override fun started() = jettyServer.isStarted

    override fun startup() {
        val context = ServletContextHandler()
        context.addLifeCycleListener(object : LifeCycle.Listener {
            override fun lifeCycleStopped(event: LifeCycle?) { /* Do nothing */ }
            override fun lifeCycleStopping(event: LifeCycle?) { /* Do nothing */ }
            override fun lifeCycleStarted(event: LifeCycle?) { /* Do nothing */ }
            override fun lifeCycleFailure(event: LifeCycle?, cause: Throwable?) { /* Do nothing */ }

            override fun lifeCycleStarting(event: LifeCycle?) {
                val filter = ServletFilter (this@JettyServletServer)
                val dispatcherTypes = EnumSet.allOf(DispatcherType::class.java)
                val filterBind = context.servletContext.addFilter("filters", filter)
                filterBind.addMappingForUrlPatterns(dispatcherTypes, true, "/*")
            }
        })

        context.sessionHandler = SessionHandler(HashSessionManager())

        jettyServer.sessionIdManager = HashSessionIdManager()
        jettyServer.handler = context
        jettyServer.start()
    }

    override fun shutdown() {
        jettyServer.stopAtShutdown = true
        jettyServer.stop()
    }
}
