package co.there4.hexagon.web.servlet

import co.there4.hexagon.web.Router
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener

import java.util.*
import javax.servlet.DispatcherType
import javax.servlet.ServletContext

/**
 * Not a standard backend as it is not started/stopped
 * TODO Take care of wildcards (review servlet specs) to group filters
 * TODO Take care of wildcards (review servlet specs) to group routes in servlets
 */
abstract class ServletServer : Router(), ServletContextListener {

    override fun contextInitialized(sce: ServletContextEvent) {
        init ()

        val filter = sce.servletContext.addFilter("filters", ServletFilter (this))
        filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType::class.java), true, "/*")
    }

    override fun contextDestroyed(sce: ServletContextEvent?) { /* Empty */ }

    abstract fun init ()
}
