package com.hexagontk.http.server

import com.hexagontk.core.Platform.charset
import com.hexagontk.core.Platform.cpuCount
import com.hexagontk.core.Platform.hostName
import com.hexagontk.core.Platform.name
import com.hexagontk.core.Platform.version
import com.hexagontk.core.Platform.localeCode
import com.hexagontk.http.model.HttpProtocol.HTTP2

import java.lang.Runtime.getRuntime
import com.hexagontk.core.text.AnsiColor.BLUE
import com.hexagontk.core.text.AnsiColor.CYAN
import com.hexagontk.core.text.AnsiColor.DEFAULT
import com.hexagontk.core.text.AnsiColor.MAGENTA
import com.hexagontk.core.text.Ansi.RESET
import com.hexagontk.core.text.AnsiEffect.BOLD
import com.hexagontk.core.text.AnsiEffect.UNDERLINE
import com.hexagontk.core.Platform.timeZone
import com.hexagontk.core.Platform.totalMemory
import com.hexagontk.core.Platform.usedMemory
import com.hexagontk.core.info
import com.hexagontk.core.loggerOf
import com.hexagontk.core.text.prependIndent
import com.hexagontk.core.urlOf
import com.hexagontk.http.HttpFeature.ZIP
import com.hexagontk.http.handlers.HttpHandler
import com.hexagontk.http.handlers.HandlerBuilder
import com.hexagontk.http.handlers.path
import java.io.Closeable
import java.lang.System.Logger
import java.lang.System.nanoTime
import java.net.URL

/**
 * Server that listen to HTTP connections on a port and address and route requests to handlers.
 *
 * TODO Allow light startup log
 */
data class HttpServer(
    private val adapter: HttpServerPort,
    val handler: HttpHandler,
    val settings: HttpServerSettings = HttpServerSettings()
) : Closeable {

    companion object {
        private val logger: Logger = loggerOf(this::class)

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
        $BLUE         \_________/       https://hexagontk.com/http_server
        $RESET
        """.trimIndent()
    }

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
    val binding: URL
        get() = urlOf("${settings.bindUrl}:$runtimePort")

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

        val hostnameValue = "$BLUE$hostName$RESET"
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
