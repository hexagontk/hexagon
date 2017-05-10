package co.there4.hexagon.server.engine.servlet

import co.there4.hexagon.server.Router

import java.util.*
import javax.servlet.DispatcherType
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener

/**
 * Not a standard engine as it is not started/stopped
 * TODO Take care of wildcards (review servlet specs) to group filters
 * TODO Take care of wildcards (review servlet specs) to group routes in servlets
 */
abstract class ServletServer : ServletContextListener {
    val serverRouter by lazy { createRouter() }

    abstract fun createRouter(): Router

    override fun contextInitialized(sce: ServletContextEvent) {
        val filter = sce.servletContext.addFilter("filters", ServletFilter (serverRouter))
        filter.setAsyncSupported(true)
        filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType::class.java), true, "/*")
    }

    override fun contextDestroyed(sce: ServletContextEvent?) { /* Empty */ }
}
