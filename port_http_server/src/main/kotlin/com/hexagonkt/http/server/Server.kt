package com.hexagonkt.http.server

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
import com.hexagonkt.http.Protocol.HTTPS
import com.hexagonkt.injection.InjectionManager.inject
import com.hexagonkt.serialization.convertToObject
import com.hexagonkt.settings.SettingsManager

import java.lang.Runtime.getRuntime
import java.lang.management.ManagementFactory.getMemoryMXBean
import java.lang.management.ManagementFactory.getRuntimeMXBean

/**
 * A server that listen to HTTP connections on a port and address and route requests using a
 * router.
 *
 * TODO Write documentation.
 */
data class Server(
    private val adapter: ServerPort = inject(),
    private val router: Router,
    val settings: ServerSettings = ServerSettings()
) {

    private val log: Logger = Logger(this)

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
        settings: Map<String, *> = SettingsManager.settings,
        block: Router.() -> Unit):
            this(adapter, Router(block), settings)

    constructor(adapter: ServerPort, router: Router, settings: Map<String, *>) :
        this (adapter, router, settings.convertToObject(ServerSettings::class))

    val runtimePort
        get() = if (started()) adapter.runtimePort() else error("Server is not running")

    val portName: String = adapter.javaClass.simpleName

    fun started(): Boolean = adapter.started()

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
        log.info { "${settings.serverName} started${createBanner()}" }
    }

    fun stop() {
        adapter.shutdown ()
        log.info { "${settings.serverName} stopped" }
    }

    private fun createBanner(): String {
        val heap = getMemoryMXBean().heapMemoryUsage
        val jvmMemory = "%,d".format(heap.init / 1024)
        val usedMemory = "%,d".format(heap.used / 1024)
        val bootTime = "%01.3f".format(getRuntimeMXBean().uptime / 1e3)
        val bindAddress = settings.bindAddress
        val protocol = settings.protocol
        val hostName = if (bindAddress.isAnyLocalAddress) ip else bindAddress.canonicalHostName
        val scheme = if (protocol == HTTPS) "https" else "http"

        val information = """
            SERVICE:     ${settings.serverName}
            SERVER TYPE: $portName

            Running in '$hostname' with $cpuCount CPUs $jvmMemory KB
            Java $version [$name]
            Locale $locale Timezone $timezone Charset $charset

            Started in $bootTime s using $usedMemory KB
            Served at $scheme://$hostName:$runtimePort${if (protocol == HTTP2) " (HTTP/2)" else ""}
        """

        // TODO Load banner from ${serverName}.txt
        // TODO Do not trim the banner (it could break ASCII art ;)
        val bannerResource = settings.serverName.toLowerCase().replace(' ', '_')
        val banner = (Resource("$bannerResource.txt").readText() ?: "") + information
        return banner
            .trimIndent()
            .lines()
            .joinToString(eol, eol + eol, eol) { " ".repeat(4) + it.trim() }
    }
}
