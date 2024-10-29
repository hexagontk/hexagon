package com.hexagontk.http.server.servlet

import com.hexagontk.core.text.AnsiColor.BLUE
import com.hexagontk.core.text.AnsiEffect.BOLD
import com.hexagontk.core.text.AnsiColor.CYAN
import com.hexagontk.core.text.AnsiColor.MAGENTA
import com.hexagontk.core.text.Ansi.RESET
import com.hexagontk.core.Platform
import com.hexagontk.core.info
import com.hexagontk.core.loggerOf
import com.hexagontk.core.text.prependIndent
import com.hexagontk.core.require
import com.hexagontk.http.server.HttpServer
import com.hexagontk.http.server.HttpServerSettings
import com.hexagontk.http.handlers.HttpHandler
import com.hexagontk.http.handlers.OnHandler
import jakarta.servlet.*
import java.lang.System.Logger
import java.lang.management.ManagementFactory
import java.util.*

/**
 * Adapter to run a router inside a Servlets container. It is not a standard engine as it is not
 * started/stopped (not passed to an [HttpServer]).
 */
abstract class ServletServer(
    private val handler: HttpHandler = OnHandler { this },
    private val settings: HttpServerSettings = HttpServerSettings(),
) : ServletContextListener {

    private companion object {
        val logger: Logger = loggerOf(ServletServer::class)
    }

    override fun contextInitialized(sce: ServletContextEvent) {
        val startTimestamp = System.nanoTime()

        val servletFilter = ServletFilter(handler)
        // Let's be a good JEE citizen
        val servletContext = sce.servletContext
        servletFilter.init(object : FilterConfig {
            val params = Hashtable<String, String>(1).apply { put("filterName", filterName) }
            override fun getFilterName(): String = ServletFilter::class.java.name
            override fun getServletContext(): ServletContext = servletContext
            override fun getInitParameter(name: String): String = params.require(name)
            override fun getInitParameterNames(): Enumeration<String> = params.keys()
        })
        val filter = servletContext.addFilter("filters", servletFilter)
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

        val hostnameValue = "$BLUE${Platform.hostName}$RESET"
        val cpuCountValue = "$BLUE${Platform.cpuCount}$RESET"
        val jvmMemoryValue = "$BLUE$jvmMemory$RESET"

        val javaVersionValue =
            "$BOLD${BLUE}Java ${Platform.version}$RESET [$BLUE${Platform.name}$RESET]"

        val localeValue = "$BLUE${Platform.localeCode}$RESET"
        val timezoneValue = "$BLUE${Platform.timeZone.id}$RESET"
        val charsetValue = "$BLUE${Platform.charset}$RESET"

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
