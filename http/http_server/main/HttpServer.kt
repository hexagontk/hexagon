package com.hexagontk.http.server

import com.hexagontk.core.Platform.charset
import com.hexagontk.core.Platform.cpuCount
import com.hexagontk.core.Platform.hostName
import com.hexagontk.core.Platform.name
import com.hexagontk.core.Platform.version
import com.hexagontk.core.Platform.localeCode

import java.lang.Runtime.getRuntime
import com.hexagontk.core.text.AnsiColor.BLUE
import com.hexagontk.core.text.AnsiColor.CYAN
import com.hexagontk.core.text.AnsiColor.MAGENTA
import com.hexagontk.core.text.Ansi.RESET
import com.hexagontk.core.text.AnsiEffect.BOLD
import com.hexagontk.core.text.AnsiEffect.UNDERLINE
import com.hexagontk.core.Platform.timeZone
import com.hexagontk.core.Platform.totalMemory
import com.hexagontk.core.Platform.usedMemory
import com.hexagontk.core.text.prependIndent
import com.hexagontk.http.HttpFeature.ZIP
import com.hexagontk.http.handlers.HttpHandler
import com.hexagontk.http.handlers.HandlerBuilder
import com.hexagontk.http.handlers.path
import java.io.Closeable
import java.net.URI

/**
 * Server that listen to HTTP connections on a port and address and route requests to handlers.
 *
 * TODO Allow light startup log
 */
class HttpServer(
    private val adapter: HttpServerPort,
    val handler: HttpHandler,
    val settings: HttpServerSettings = HttpServerSettings()
) : Closeable {

    /**
     * Create a server with a builder ([HandlerBuilder]) to set up handlers.
     *
     * @param adapter The server engine.
     * @param settings Server settings. Port and address will be searched in this map.
     * @param block Handlers' setup block.
     * @return A new server with the configured handlers.
     */
    constructor(
        adapter: HttpServerPort,
        settings: HttpServerSettings = HttpServerSettings(),
        block: HandlerBuilder.() -> Unit
    ) :
        this(adapter, path(block = block), settings)

    override fun close() {
        stop()
    }

    init {
        val supportedProtocols = adapter.supportedProtocols()
        check(settings.protocol in supportedProtocols) {
            val supportedProtocolsText = supportedProtocols.joinToString(", ")
            "Requesting unsupported protocol. Adapter's protocols: $supportedProtocolsText"
        }

        if (settings.zip)
            check(adapter.supportedFeatures().contains(ZIP)) {
                val adapterName = adapter::class.qualifiedName
                "Requesting ZIP compression with an adapter without support: '$adapterName'"
            }
    }

    /**
     * Runtime port of the server.
     *
     * @exception IllegalStateException Throw an exception if the server hasn't been started.
     */
    val runtimePort: Int
        get() = if (started()) adapter.runtimePort() else error("Server is not running")

    /**
     * Runtime binding of the server.
     *
     * @exception IllegalStateException Throw an exception if the server hasn't been started.
     */
    val binding: URI
        get() = URI("${settings.bindUrl}:$runtimePort")

    /**
     * The port name of the server.
     */
    val portName: String = adapter.javaClass.simpleName

    /**
     * Check whether the server has been started.
     *
     * @return True if the server has started, else false.
     */
    fun started(): Boolean =
        adapter.started()

    /**
     * Start the server with the adapter instance and adds a shutdown hook for stopping the server.
     */
    fun start() {
        getRuntime().addShutdownHook(
            Thread(
                {
                    if (started())
                        adapter.shutDown()
                },
                "shutdown-${settings.bindAddress.hostName}-${settings.bindPort}"
            )
        )

        adapter.startUp(this)
    }

    /**
     * Stop the server.
     */
    fun stop() {
        adapter.shutDown()
    }

    fun createBanner(
        startUpTimestamp: Long = -1, banner: String = serverBanner, detailed: Boolean = false
    ): String {
        val server = "$BOLD$CYAN$portName$RESET"
        val protocol = settings.protocol
        val protocols = adapter.supportedProtocols()
            .joinToString("$RESET, $CYAN", CYAN, RESET) { if (it == protocol) "✅$it" else "$it" }

        val java = "$BOLD${BLUE}Java $version$RESET [$BLUE$name$RESET]"
        val locale = "$BLUE$localeCode$RESET"
        val timezone = "$BLUE${timeZone.id}$RESET"
        val charsetValue = "$BLUE$charset$RESET"
        val start = if (startUpTimestamp < 0) "<undefined>" else startUpTimestamp.toString()
        val startTime = "$BOLD$MAGENTA in $start ms$RESET"
        val bindingValue = "$BLUE$UNDERLINE$binding$RESET"

        val information = if (detailed)
            detailBanner(
                server, protocols, java, locale, timezone, charsetValue, startTime, bindingValue
            )
        else
            """

            Server Adapter: $server ($protocols)

            🛠 Using $java
            🌍 Locale: $locale Timezone: $timezone Charset: $charset

            ⏱️ Started$startTime
            🚀 Served at $bindingValue

            """

        val fullBanner = banner + information.trimIndent()
        return "\n" + fullBanner.prependIndent()
    }

    private fun detailBanner(
        server: String,
        protocols: String,
        java: String,
        locale: String,
        timezone: String,
        charsetValue: String,
        startTime: String,
        bindingValue: String
    ): String {
        val features = adapter.supportedFeatures()
            .joinToString("$RESET, $CYAN", CYAN, RESET) { it.toString() }

        val options = adapter.options()
            .map { (k, v) -> "$k($v)" }
            .joinToString("$RESET, $CYAN", CYAN, RESET)

        val jvmMemoryValue = "$BLUE${totalMemory()} KB$RESET"
        val usedMemoryValue = "$BOLD$MAGENTA${usedMemory()} KB$RESET"
        val hostnameValue = "$BLUE$hostName$RESET"
        val cpuCountValue = "$BLUE$cpuCount$RESET"

        return """

            Server Adapter: $server ($protocols)
            Supported Features: $features
            Configuration Options: $options

            🖥️️ Running in '$hostnameValue' with $cpuCountValue CPUs $jvmMemoryValue of memory
            🛠 Using $java
            🌍 Locale: $locale Timezone: $timezone Charset: $charsetValue

            ⏱️ Started$startTime using $usedMemoryValue
            🚀 Served at $bindingValue

            """
    }
}
