package com.hexagonkt.http.server.servlet

import com.hexagonkt.http.server.Router
import com.hexagonkt.http.server.ServerFeature.SESSIONS
import com.hexagonkt.http.server.ServerSettings

import java.util.*
import javax.servlet.DispatcherType
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener

/**
 * Adapter to run a router inside a Servlets container. It is not a standard engine as it is not
 * started/stopped.
 */
abstract class ServletServer(
    protected val router: Router = Router(),
    private val async: Boolean = false
) : ServletContextListener {

    override fun contextInitialized(sce: ServletContextEvent) {
        val serverSettings = ServerSettings(features = setOf(SESSIONS))
        val servletFilter = ServletFilter(router.flatRequestHandlers(), serverSettings)
        val filter = sce.servletContext.addFilter("filters", servletFilter)
        filter.setAsyncSupported(async)
        filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType::class.java), true, "/*")
    }

    override fun contextDestroyed(sce: ServletContextEvent?) { /* Empty */ }
}
