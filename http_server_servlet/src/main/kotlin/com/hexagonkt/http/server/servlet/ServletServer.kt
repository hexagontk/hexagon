package com.hexagonkt.http.server.servlet

import com.hexagonkt.core.Ansi.BLUE
import com.hexagonkt.core.Ansi.BOLD
import com.hexagonkt.core.Ansi.CYAN
import com.hexagonkt.core.Ansi.MAGENTA
import com.hexagonkt.core.Ansi.RESET
import com.hexagonkt.core.Jvm
import com.hexagonkt.core.prependIndent
import com.hexagonkt.core.require
import com.hexagonkt.core.logging.Logger
import com.hexagonkt.http.server.HttpServer
import com.hexagonkt.http.server.HttpServerSettings
import com.hexagonkt.http.server.handlers.PathHandler
import com.hexagonkt.http.server.handlers.HttpHandler
import com.hexagonkt.http.server.handlers.path
import jakarta.servlet.*
import java.lang.management.ManagementFactory
import java.util.*

/**
 * Adapter to run a router inside a Servlets container. It is not a standard engine as it is not
 * started/stopped (not passed to an [HttpServer]).
 */
abstract class ServletServer(
    handlers: List<HttpHandler> = emptyList(),
    private val settings: HttpServerSettings = HttpServerSettings(),
) : ServletContextListener {

    private val logger: Logger = Logger(ServletServer::class)

    private val pathHandler: PathHandler = path(settings.contextPath, handlers)

    /**
     * Utility constructor for the common case of having a single root handler.
     *
     * @param handler The only handler used for this server.
     * @param settings Settings used by this server.
     */
    constructor(
        handler: HttpHandler,
        settings: HttpServerSettings = HttpServerSettings(),
    ) : this(listOf(handler), settings)

    override fun contextInitialized(sce: ServletContextEvent) {
        val startTimestamp = System.nanoTime()

        val servletFilter = ServletFilter(pathHandler, settings)
        // Let's be a good JEE citizen
        servletFilter.init(object : FilterConfig {
            val params = Hashtable<String, String>(1).apply { put("filterName", filterName) }
            override fun getFilterName(): String = ServletFilter::class.java.name
            override fun getServletContext(): ServletContext = sce.servletContext
            override fun getInitParameter(name: String): String = params.require(name)
            override fun getInitParameterNames(): Enumeration<String> = params.keys()
        })
        val filter = sce.servletContext.addFilter("filters", servletFilter)
        filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType::class.java), true, "/*")

        logger.info { "Server started\n${createBanner(System.nanoTime() - startTimestamp)}" }
    }

    override fun contextDestroyed(sce: ServletContextEvent?) {
        logger.info { "Server context destroyed" }
    }

    private fun createBanner(startUpTimestamp: Long): String {

        val heap = ManagementFactory.getMemoryMXBean().heapMemoryUsage
        val jvmMemory = "%,d".format(heap.init / 1024)
        val usedMemory = "%,d".format(heap.used / 1024)
        val bootTime = "%01.3f".format(ManagementFactory.getRuntimeMXBean().uptime / 1e3)
        val startUpTime = "%,.0f".format(startUpTimestamp / 1e6)

        val serverAdapterValue = "$BOLD$CYAN${javaClass.simpleName}$RESET"

        val hostnameValue = "$BLUE${Jvm.hostname}$RESET"
        val cpuCountValue = "$BLUE${Jvm.cpuCount}$RESET"
        val jvmMemoryValue = "$BLUE$jvmMemory$RESET"

        val javaVersionValue = "$BOLD${BLUE}Java ${Jvm.version}$RESET [$BLUE${Jvm.name}$RESET]"

        val localeValue = "$BLUE${Jvm.localeCode}$RESET"
        val timezoneValue = "$BLUE${Jvm.timezone}$RESET"
        val charsetValue = "$BLUE${Jvm.charset}$RESET"

        val bootTimeValue = "$BOLD$MAGENTA$bootTime s$RESET"
        val startUpTimeValue = "$BOLD$MAGENTA$startUpTime ms$RESET"
        val usedMemoryValue = "$BOLD$MAGENTA$usedMemory KB$RESET"

        val information = """

            Server Adapter: $serverAdapterValue

            🖥️️ Running in '$hostnameValue' with $cpuCountValue CPUs $jvmMemoryValue KB
            🛠 Using $javaVersionValue
            🌍 Locale: $localeValue Timezone: $timezoneValue Charset: $charsetValue

            ⏱ Started in $bootTimeValue (server: $startUpTimeValue) using $usedMemoryValue
            🚀 Served at a JEE Server

        """.trimIndent()

        val banner = (settings.banner?.let { "$it\n" } ?: HttpServer.banner) + information
        return banner.prependIndent()
    }
}
