package com.hexagonkt.http.server

import com.hexagonkt.core.logging.Logger
import com.hexagonkt.core.helpers.*
import com.hexagonkt.core.helpers.Jvm.charset
import com.hexagonkt.core.helpers.Jvm.cpuCount
import com.hexagonkt.core.helpers.Jvm.hostname
import com.hexagonkt.core.helpers.Jvm.ip
import com.hexagonkt.core.helpers.Jvm.name
import com.hexagonkt.core.helpers.Jvm.version
import com.hexagonkt.core.helpers.Jvm.localeCode
import com.hexagonkt.core.helpers.Jvm.timezone
import com.hexagonkt.http.model.HttpProtocol.HTTP2
import com.hexagonkt.http.model.HttpProtocol.HTTP

import java.lang.Runtime.getRuntime
import java.lang.management.ManagementFactory.getMemoryMXBean
import java.lang.management.ManagementFactory.getRuntimeMXBean
import com.hexagonkt.core.helpers.Ansi.BLUE
import com.hexagonkt.core.helpers.Ansi.BOLD
import com.hexagonkt.core.helpers.Ansi.CYAN
import com.hexagonkt.core.helpers.Ansi.DEFAULT
import com.hexagonkt.core.helpers.Ansi.MAGENTA
import com.hexagonkt.core.helpers.Ansi.RESET
import com.hexagonkt.core.helpers.Ansi.UNDERLINE
import com.hexagonkt.http.server.handlers.PathBuilder
import com.hexagonkt.http.server.handlers.ServerHandler
import com.hexagonkt.http.server.handlers.path
import java.io.Closeable
import java.lang.System.nanoTime

/**
 * Server that listen to HTTP connections on a port and address and route requests to handlers.
 */
data class HttpServer(
    private val adapter: HttpServerPort,
    val handlers: List<ServerHandler>,
    val settings: HttpServerSettings = HttpServerSettings()
) : Closeable {

    companion object {
        val banner: String = """
        $CYAN          _________
        $CYAN         /         \
        $CYAN        /   ____   /
        $CYAN       /   /   /  /
        $CYAN      /   /   /__/$BLUE   /\$BOLD    H E X A G O N$RESET
        $CYAN     /   /$BLUE          /  \$DEFAULT        ___
        $CYAN     \  /$BLUE   ___    /   /
        $CYAN      \/$BLUE   /  /   /   /$CYAN    T O O L K I T$RESET
        $BLUE          /  /___/   /
        $BLUE         /          /
        $BLUE         \_________/       https://hexagonkt.com/http_server
        $RESET
        """.trimIndent()
    }

    private val logger: Logger = Logger(this::class)

    /**
     * Create a server with a builder ([PathBuilder]) to set up handlers.
     *
     * @param adapter The server engine.
     * @param settings Server settings. Port and address will be searched in this map.
     * @param block Handlers' setup block.
     * @return A new server with the configured handlers.
     */
    constructor(
        adapter: HttpServerPort,
        settings: HttpServerSettings = HttpServerSettings(),
        block: PathBuilder.() -> Unit
    ) :
        this(adapter, listOf(path(block = block)), settings)

    /**
     * Utility constructor for the common case of having a single root handler.
     *
     * @param adapter The server engine.
     * @param handler The only handler used for this server.
     * @param settings Server settings. Port and address will be searched in this map.
     */
    constructor(
        adapter: HttpServerPort,
        handler: ServerHandler,
        settings: HttpServerSettings = HttpServerSettings(),
    ) : this(adapter, listOf(handler), settings)

    override fun close() {
        stop()
    }

    init {
        val supportedProtocols = adapter.supportedProtocols()
        check(settings.protocol in supportedProtocols) {
            val supportedProtocolsText = supportedProtocols.joinToString(", ")
            "Requesting unsupported protocol. Adapter's protocols: $supportedProtocolsText"
        }

        val supportedFeatures = adapter.supportedFeatures()
        check(settings.features.all { it in supportedFeatures }) {
            val supportedFeaturesText = supportedFeatures.joinToString(", ")
            "Requesting unsupported feature. Adapter's features: $supportedFeaturesText"
        }

        val supportedOptions = adapter.supportedOptions()
        check(settings.options.keys.all { it in supportedOptions }) {
            val supportedOptionsText = supportedOptions.joinToString(", ")
            "Setting unsupported option. Adapter's options: $supportedOptionsText"
        }
    }

    /**
     * Runtime port of the server.
     *
     * @exception IllegalStateException Throw an exception if the server hasn't been started.
     */
    val runtimePort
        get() = if (started()) adapter.runtimePort() else error("Server is not running")

    /**
     * The port name of the server.
     */
    val portName: String = adapter.javaClass.simpleName

    /**
     * Check whether the server has been started.
     *
     * @return True if the server has started, else false.
     */
    fun started(): Boolean = adapter.started()

    /**
     * Start the server with the adapter instance and adds a shutdown hook for stopping the server.
     */
    fun start() {
        val startTimestamp = nanoTime()

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
        logger.info { "Server started\n${createBanner(nanoTime() - startTimestamp)}" }
    }

    /**
     * Stop the server.
     */
    fun stop() {
        adapter.shutDown()
        logger.info { "Server stopped" }
    }

    internal fun createBanner(startUpTimestamp: Long): String {

        val heap = getMemoryMXBean().heapMemoryUsage
        val jvmMemory = "%,d".format(heap.init / 1024)
        val usedMemory = "%,d".format(heap.used / 1024)
        val bootTime = "%01.3f".format(getRuntimeMXBean().uptime / 1e3)
        val startUpTime = "%,.0f".format(startUpTimestamp / 1e6)
        val bindAddress = settings.bindAddress
        val protocol = settings.protocol
        val hostName = if (bindAddress.isAnyLocalAddress) ip else bindAddress.canonicalHostName
        val scheme = if (protocol == HTTP) "http" else "https"
        val binding = "$scheme://$hostName:$runtimePort"

        val serverAdapterValue = "$BOLD$CYAN$portName$RESET"

        val protocols = adapter.supportedProtocols()
            .joinToString("$RESET, $CYAN", CYAN, RESET) {
                if (it == settings.protocol) "✅$it" else "$it"
            }

        val features = adapter.supportedFeatures()
            .joinToString("$RESET, $CYAN", CYAN, RESET) {
                if (settings.features.contains(it)) "✅$it" else "$it"
            }

        val options = adapter.supportedOptions()
            .joinToString("$RESET, $CYAN", CYAN, RESET) {
                settings.options[it]?.let { option -> "$it($option)" } ?: it
            }

        val hostnameValue = "$BLUE$hostname$RESET"
        val cpuCountValue = "$BLUE$cpuCount$RESET"
        val jvmMemoryValue = "$BLUE$jvmMemory$RESET"

        val javaVersionValue = "$BOLD${BLUE}Java $version$RESET [$BLUE$name$RESET]"

        val localeValue = "$BLUE$localeCode$RESET"
        val timezoneValue = "$BLUE$timezone$RESET"
        val charsetValue = "$BLUE$charset$RESET"

        val bootTimeValue = "$BOLD$MAGENTA$bootTime s$RESET"
        val startUpTimeValue = "$BOLD$MAGENTA$startUpTime ms$RESET"
        val usedMemoryValue = "$BOLD$MAGENTA$usedMemory KB$RESET"
        val bindingValue = "$BLUE$UNDERLINE$binding$RESET"

        val information = """

            Server Adapter: $serverAdapterValue ($protocols)
            Supported Features: $features
            Configuration Options: $options

            🖥️️ Running in '$hostnameValue' with $cpuCountValue CPUs $jvmMemoryValue KB
            🛠 Using $javaVersionValue
            🌍 Locale: $localeValue Timezone: $timezoneValue Charset: $charsetValue

            ⏱ Started in $bootTimeValue (server: $startUpTimeValue) using $usedMemoryValue
            🚀 Served at $bindingValue${if (protocol == HTTP2) " (HTTP/2)" else "" }

        """.trimIndent()

        val banner = (settings.banner?.let { "$it\n" } ?: banner) + information
        return banner.prependIndent()
    }
}
