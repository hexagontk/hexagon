package com.hexagonkt.http.server

import com.hexagonkt.logging.Logger
import com.hexagonkt.helpers.*
import com.hexagonkt.helpers.Jvm.charset
import com.hexagonkt.helpers.Jvm.cpuCount
import com.hexagonkt.helpers.Jvm.hostname
import com.hexagonkt.helpers.Jvm.ip
import com.hexagonkt.helpers.Jvm.name
import com.hexagonkt.helpers.Jvm.version
import com.hexagonkt.helpers.Jvm.locale
import com.hexagonkt.helpers.Jvm.timezone
import com.hexagonkt.http.Protocol.HTTP2
import com.hexagonkt.http.Protocol.HTTP
import com.hexagonkt.injection.InjectionManager.inject
import com.hexagonkt.injection.InjectionManager.injectOrNull

import java.lang.Runtime.getRuntime
import java.lang.management.ManagementFactory.getMemoryMXBean
import java.lang.management.ManagementFactory.getRuntimeMXBean
import com.hexagonkt.helpers.Ansi.BLUE
import com.hexagonkt.helpers.Ansi.BOLD
import com.hexagonkt.helpers.Ansi.CYAN
import com.hexagonkt.helpers.Ansi.DEFAULT
import com.hexagonkt.helpers.Ansi.MAGENTA
import com.hexagonkt.helpers.Ansi.RESET
import com.hexagonkt.helpers.Ansi.UNDERLINE

/**
 * A server that listen to HTTP connections on a port and address and route requests using a
 * router.
 */
data class Server(
    private val adapter: ServerPort = inject(),
    private val router: Router,
    val settings: ServerSettings = ServerSettings()
) {

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
        $BLUE         \_________/       https://hexagonkt.com/port_http_server
        $RESET
        """.trimIndent()
    }

    private val log: Logger = Logger(this::class)

    /**
     * Provides a [Router] instance configured with the context path in [ServerSettings].
     */
    val contextRouter: Router by lazy {
        if (settings.contextPath.isEmpty())
            router
        else
            Router { path(settings.contextPath, router) }
    }

    /**
     * Creates a server with a router. It is a combination of [Server] and [Router].
     *
     * @param adapter The server engine.
     * @param settings Server settings. Port and address will be searched in this map.
     * @param block Router's setup block.
     * @return A new server with the built router.
     */
    constructor(
        adapter: ServerPort = inject(),
        settings: ServerSettings = injectOrNull() ?: ServerSettings(),
        block: Router.() -> Unit):
            this(adapter, Router(block), settings)

    /**
     * The runtime port of the server.
     * @exception IllegalStateException Throws exception if the server hasn't been started.
     */
    val runtimePort
        get() = if (started()) adapter.runtimePort() else error("Server is not running")

    /**
     * The port name of the server.
     */
    val portName: String = adapter.javaClass.simpleName

    /**
     * Checks whether the server has been started.
     *
     * @return True if the server has started, else false.
     */
    fun started(): Boolean = adapter.started()

    /**
     * Starts the server with the adapter instance and
     * adds a shutdown hook for stopping the server.
     */
    fun start() {
        getRuntime().addShutdownHook(
            Thread (
                {
                    if (started ())
                        adapter.shutdown ()
                },
                "shutdown-${settings.bindAddress.hostName}-${settings.bindPort}"
            )
        )

        adapter.startup (this)
        log.info { "Server started\n${createBanner()}" }
    }

    /**
     * Stops the server.
     */
    fun stop() {
        adapter.shutdown ()
        log.info { "Server stopped" }
    }

    private fun createBanner(): String {
        val heap = getMemoryMXBean().heapMemoryUsage
        val jvmMemory = "%,d".format(heap.init / 1024)
        val usedMemory = "%,d".format(heap.used / 1024)
        val bootTime = "%01.3f".format(getRuntimeMXBean().uptime / 1e3)
        val bindAddress = settings.bindAddress
        val protocol = settings.protocol
        val hostName = if (bindAddress.isAnyLocalAddress) ip else bindAddress.canonicalHostName
        val scheme = if (protocol == HTTP) "http" else "https"
        val binding = "$scheme://$hostName:$runtimePort"

        val serverAdapterValue = "$BOLD$CYAN$portName$RESET"

        val hostnameValue = "$BLUE$hostname$RESET"
        val cpuCountValue = "$BLUE$cpuCount$RESET"
        val jvmMemoryValue = "$BLUE$jvmMemory$RESET"

        val javaVersionValue = "$BOLD${BLUE}Java $version$RESET [$BLUE$name$RESET]"

        val localeValue = "$BLUE$locale$RESET"
        val timezoneValue = "$BLUE$timezone$RESET"
        val charsetValue = "$BLUE$charset$RESET"

        val bootTimeValue = "$BOLD$MAGENTA$bootTime s$RESET"
        val usedMemoryValue = "$BOLD$MAGENTA$usedMemory KB$RESET"
        val bindingValue = "$BLUE$UNDERLINE$binding$RESET"

        val information = """

            Server Adapter: $serverAdapterValue

            Running in '$hostnameValue' with $cpuCountValue CPUs $jvmMemoryValue KB
            Using $javaVersionValue
            Locale: $localeValue Timezone: $timezoneValue Charset: $charsetValue

            Started in $bootTimeValue using $usedMemoryValue
            Served at $bindingValue${if (protocol == HTTP2) " (HTTP/2)" else ""}
        """.trimIndent()

        val banner = (settings.banner?.let { "$it\n" } ?: banner ) + information
        return banner.indent()
    }
}
