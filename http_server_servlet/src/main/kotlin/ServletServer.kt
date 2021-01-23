package com.hexagonkt.http.server.servlet

import com.hexagonkt.http.server.Router

import java.util.*
import javax.servlet.DispatcherType
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener

/**
 * Adapter to run a router inside a Servlets container. It is not a standard engine as it is not
 * started/stopped.
 */
abstract class ServletServer(
    private val router: Router = Router(),
    private val async: Boolean = false
) : ServletContextListener {

    private val serverRouter by lazy { createRouter() }

    open fun createRouter(): Router = router

    override fun contextInitialized(sce: ServletContextEvent) {
        val servletFilter = ServletFilter(serverRouter.flatRequestHandlers())
        val filter = sce.servletContext.addFilter("filters", servletFilter)
        filter.setAsyncSupported(async)
        filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType::class.java), true, "/*")
    }

    override fun contextDestroyed(sce: ServletContextEvent?) { /* Empty */ }
}
