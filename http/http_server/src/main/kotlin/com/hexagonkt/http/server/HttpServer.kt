package com.hexagonkt.http.server

import com.hexagonkt.core.logging.Logger
import com.hexagonkt.core.Jvm.charset
import com.hexagonkt.core.Jvm.cpuCount
import com.hexagonkt.core.Jvm.hostname
import com.hexagonkt.core.Jvm.name
import com.hexagonkt.core.Jvm.version
import com.hexagonkt.core.Jvm.localeCode
import com.hexagonkt.http.model.HttpProtocol.HTTP2

import java.lang.Runtime.getRuntime
import com.hexagonkt.core.Ansi.BLUE
import com.hexagonkt.core.Ansi.BOLD
import com.hexagonkt.core.Ansi.CYAN
import com.hexagonkt.core.Ansi.DEFAULT
import com.hexagonkt.core.Ansi.MAGENTA
import com.hexagonkt.core.Ansi.RESET
import com.hexagonkt.core.Ansi.UNDERLINE
import com.hexagonkt.core.Jvm.timeZone
import com.hexagonkt.core.Jvm.totalMemory
import com.hexagonkt.core.Jvm.usedMemory
import com.hexagonkt.core.prependIndent
import com.hexagonkt.http.server.HttpServerFeature.ZIP
import com.hexagonkt.http.handlers.HttpHandler
import com.hexagonkt.http.handlers.HandlerBuilder
import com.hexagonkt.http.handlers.path
import java.io.Closeable
import java.lang.System.nanoTime

/**
 * Server that listen to HTTP connections on a port and address and route requests to handlers.
 */
data class HttpServer(
    private val adapter: HttpServerPort,
    val handler: HttpHandler,
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
    fun started(): Boolean =
        adapter.started()

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
        logger.info { "Server started${createBanner(nanoTime() - startTimestamp)}" }
    }

    /**
     * Stop the server.
     */
    fun stop() {
        adapter.shutDown()
        logger.info { "Server stopped" }
    }

    internal fun createBanner(startUpTimestamp: Long): String {

        val startUpTime = "%,.0f".format(startUpTimestamp / 1e6)
        val protocol = settings.protocol
        val binding = "${settings.base}:$runtimePort"
        val banner = settings.banner ?: return " at $binding ($startUpTime ms)"

        val jvmMemoryValue = "$BLUE${totalMemory()} KB$RESET"
        val usedMemoryValue = "$BOLD$MAGENTA${usedMemory()} KB$RESET"
        val serverAdapterValue = "$BOLD$CYAN$portName$RESET"

        val protocols = adapter.supportedProtocols()
            .joinToString("$RESET, $CYAN", CYAN, RESET) { if (it == protocol) "✅$it" else "$it" }

        val features = adapter.supportedFeatures()
            .joinToString("$RESET, $CYAN", CYAN, RESET) { it.toString() }

        val options = adapter.options()
            .map { (k, v) -> "$k($v)" }
            .joinToString("$RESET, $CYAN", CYAN, RESET)

        val hostnameValue = "$BLUE$hostname$RESET"
        val cpuCountValue = "$BLUE$cpuCount$RESET"

        val javaVersionValue = "$BOLD${BLUE}Java $version$RESET [$BLUE$name$RESET]"

        val localeValue = "$BLUE$localeCode$RESET"
        val timezoneValue = "$BLUE${timeZone.id}$RESET"
        val charsetValue = "$BLUE$charset$RESET"

        val startUpTimeValue = "$BOLD$MAGENTA$startUpTime ms$RESET"
        val bindingValue = "$BLUE$UNDERLINE$binding$RESET"

        val information =
            """

            Server Adapter: $serverAdapterValue ($protocols)
            Supported Features: $features
            Configuration Options: $options

            🖥️️ Running in '$hostnameValue' with $cpuCountValue CPUs $jvmMemoryValue of memory
            🛠 Using $javaVersionValue
            🌍 Locale: $localeValue Timezone: $timezoneValue Charset: $charsetValue

            ⏱️ Started in $startUpTimeValue (excluding VM) using $usedMemoryValue
            🚀 Served at $bindingValue${if (protocol == HTTP2) " (HTTP/2)" else "" }

            """

        val fullBanner = banner + information.trimIndent()
        return "\n" + fullBanner.prependIndent()
    }
}
