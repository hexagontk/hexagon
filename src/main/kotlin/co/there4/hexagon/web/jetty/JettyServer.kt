package co.there4.hexagon.web.jetty

import org.eclipse.jetty.server.Server as JettyServletServer
import org.eclipse.jetty.server.session.HashSessionIdManager
import org.eclipse.jetty.server.session.HashSessionManager
import org.eclipse.jetty.server.session.SessionHandler
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.util.component.LifeCycle

import co.there4.hexagon.web.servlet.ServletServer
import org.eclipse.jetty.server.ServerConnector

import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.InetAddress.getByName as address

/**
 * @author jam
 */
class JettyServer (
    bindAddress: InetAddress = address ("localhost"),
    bindPort: Int = 4321,
    keystore: String? = null,
    keystorePassword: String? = null,
    truststore: String? = null,
    truststorePassword: String? = null): ServletServer (
    bindAddress, bindPort, keystore, keystorePassword, truststore, truststorePassword) {

    val jettyServer = JettyServletServer(InetSocketAddress(bindAddress, bindPort))

    override val localPort: Int get() = (jettyServer.connectors[0] as ServerConnector).port

    override fun init() { /* Do nothing */ }

    override fun started() = jettyServer.isStarted

    override fun startup() {
        val context = ServletContextHandler()
        context.sessionHandler = SessionHandler(HashSessionManager())

        jettyServer.sessionIdManager = HashSessionIdManager()
        jettyServer.handler = context

        context.addLifeCycleListener(object : LifeCycle.Listener {
            override fun lifeCycleStopped(event: LifeCycle?) { /* Do nothing */ }
            override fun lifeCycleStopping(event: LifeCycle?) { /* Do nothing */ }
            override fun lifeCycleStarted(event: LifeCycle?) { /* Do nothing */ }
            override fun lifeCycleFailure(event: LifeCycle?, cause: Throwable?) { /* Do nothing */ }

            override fun lifeCycleStarting(event: LifeCycle?) {
                initializeContext (context.servletContext)
            }
        })

        jettyServer.start()
    }

    override fun shutdown() {
        jettyServer.stopAtShutdown = true
        jettyServer.stop()
    }
}
