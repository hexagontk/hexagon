package co.there4.hexagon.web.servlet

import java.net.InetAddress
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener

import co.there4.hexagon.web.Server
import java.util.*
import javax.servlet.DispatcherType
import javax.servlet.ServletContext

/**
 * Not a standard backend as it is not started/stopped
 * TODO Take care of wildcards (review servlet specs) to group filters
 * TODO Take care of wildcards (review servlet specs) to group routes in servlets
 */
abstract class ServletServer (
    bindAddress: InetAddress = InetAddress.getLocalHost(),
    bindPort: Int = 4321,
    keystore: String? = null,
    keystorePassword: String? = null,
    truststore: String? = null,
    truststorePassword: String? = null):
        Server (
            bindAddress,
            bindPort,
            keystore,
            keystorePassword,
            truststore,
            truststorePassword),
        ServletContextListener {

    override fun started(): Boolean = throw UnsupportedOperationException()
    override fun startup(): Unit = throw UnsupportedOperationException()
    override fun shutdown(): Unit = throw UnsupportedOperationException()

    override fun contextInitialized(sce: ServletContextEvent) {
        initializeContext(sce.servletContext)
    }

    fun initializeContext(servletContext: ServletContext) {
        init ()

        val filter = ServletFilter (this, filters, routes)
        servletContext.addFilter("filters", filter)
            .addMappingForUrlPatterns(EnumSet.allOf(DispatcherType::class.java), true, "/*")
    }

    override fun contextDestroyed(sce: ServletContextEvent?) { /* Empty */ }

    abstract fun init ()
}
