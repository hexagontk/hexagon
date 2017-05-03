package co.there4.hexagon.web.backend.servlet

import co.there4.hexagon.web.Router
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener

import java.util.*
import javax.servlet.DispatcherType

/**
 * Not a standard backend as it is not started/stopped
 * TODO Take care of wildcards (review servlet specs) to group filters
 * TODO Take care of wildcards (review servlet specs) to group routes in servlets
 */
abstract class ServletServer : ServletContextListener {
    val router = Router()

    abstract fun Router.initRoutes()

    override fun contextInitialized(sce: ServletContextEvent) {
        router.initRoutes()
        val filter = sce.servletContext.addFilter("filters", ServletFilter (router))
        filter.setAsyncSupported(true)
        filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType::class.java), true, "/*")
    }

    override fun contextDestroyed(sce: ServletContextEvent?) { /* Empty */ }
}
