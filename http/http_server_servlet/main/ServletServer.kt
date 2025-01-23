package com.hexagontk.http.server.servlet

import com.hexagontk.core.text.AnsiColor.BLUE
import com.hexagontk.core.text.AnsiEffect.BOLD
import com.hexagontk.core.text.AnsiColor.CYAN
import com.hexagontk.core.text.AnsiColor.MAGENTA
import com.hexagontk.core.text.Ansi.RESET
import com.hexagontk.core.Platform
import com.hexagontk.core.Platform.cpuCount
import com.hexagontk.core.Platform.hostName
import com.hexagontk.core.Platform.localeCode
import com.hexagontk.core.Platform.name
import com.hexagontk.core.Platform.timeZone
import com.hexagontk.core.Platform.totalMemory
import com.hexagontk.core.Platform.usedMemory
import com.hexagontk.core.Platform.version
import com.hexagontk.core.text.prependIndent
import com.hexagontk.core.require
import com.hexagontk.http.server.HttpServer
import com.hexagontk.http.server.serverBanner
import com.hexagontk.http.handlers.HttpHandler
import com.hexagontk.http.handlers.OnHandler
import jakarta.servlet.*
import java.util.*

/**
 * Adapter to run a router inside a Servlets container. It is not a standard engine as it is not
 * started/stopped (not passed to an [HttpServer]).
 */
abstract class ServletServer(
    private val handler: HttpHandler = OnHandler { this },
) : ServletContextListener {

    override fun contextInitialized(sce: ServletContextEvent) {
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
    }

    fun createBanner(
        startUpTimestamp: Long = -1, banner: String = serverBanner, detailed: Boolean = false
    ): String {
        val server = "$BOLD$CYAN${javaClass.simpleName}$RESET"
        val java = "$BOLD${BLUE}Java $version$RESET [$BLUE$name$RESET]"
        val locale = "$BLUE$localeCode$RESET"
        val timezone = "$BLUE${timeZone.id}$RESET"
        val charsetValue = "$BLUE${Platform.charset}$RESET"
        val start = if (startUpTimestamp < 0) "<undefined>" else startUpTimestamp.toString()
        val startTime = "$BOLD$MAGENTA in $start ms$RESET"

        val information = if (detailed)
            detailBanner(server, java, locale, timezone, charsetValue, startTime)
        else
            """

            Server Adapter: $server

            🛠 Using $java
            🌍 Locale: $locale Timezone: $timezone Charset: $charsetValue

            ⏱️ Started$startTime
            🚀 Served at a JEE Server

            """

        val fullBanner = banner + information.trimIndent()
        return "\n" + fullBanner.prependIndent()
    }

    private fun detailBanner(
        server: String,
        java: String,
        locale: String,
        timezone: String,
        charsetValue: String,
        startTime: String
    ): String {
        val bootTime = "%01.3f".format(Platform.uptime() / 1e3)
        val uptimeValue = "$BOLD$MAGENTA$bootTime s$RESET"
        val jvmMemoryValue = "$BLUE${totalMemory()} KB$RESET"
        val usedMemoryValue = "$BOLD$MAGENTA${usedMemory()} KB$RESET"
        val hostnameValue = "$BLUE$hostName$RESET"
        val cpuCountValue = "$BLUE$cpuCount$RESET"

        return """

            Server Adapter: $server

            🖥️️ Running in '$hostnameValue' with $cpuCountValue CPUs $jvmMemoryValue of memory
            🛠 Using $java
            🌍 Locale: $locale Timezone: $timezone Charset: $charsetValue

            ⏱️ Started$startTime (uptime: $uptimeValue) using $usedMemoryValue
            🚀 Served at a JEE Server

            """
    }
}
