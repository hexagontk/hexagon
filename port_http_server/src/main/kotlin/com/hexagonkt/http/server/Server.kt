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
import com.hexagonkt.settings.SettingsManager

import java.lang.Runtime.getRuntime
import java.lang.management.ManagementFactory.getMemoryMXBean
import java.lang.management.ManagementFactory.getRuntimeMXBean
import java.net.InetAddress
import java.net.InetAddress.getByName as address

/**
 * A server that listen to HTTP connections on a port and address and route requests using a
 * router.
 *
 * TODO Write documentation.
 */
data class Server (
    private val serverEngine: ServerPort,
    val router: Router,
    val serverName: String = Server.DEFAULT_NAME,
    val bindAddress: InetAddress = address(Server.DEFAULT_ADDRESS),
    val bindPort: Int = Server.DEFAULT_PORT
) {

    internal companion object {
        internal const val DEFAULT_NAME = "<undefined>"
        internal const val DEFAULT_ADDRESS = "127.0.0.1"
        internal const val DEFAULT_PORT = 2010
    }

    private val log: Logger = Logger(this)

    /**
     * Creates a server with a router. It is a combination of [Server] and [router].
     *
     * @param engine The server engine.
     * @param settings Server settings. Port and address will be searched in this map.
     * @param block Router's setup block.
     * @return A new server with the built router.
     */
    constructor(
        engine: ServerPort,
        settings: Map<String, *> = SettingsManager.settings,
        block: Router.() -> Unit):
            this(engine, Router(block), settings)

    constructor(serverEngine: ServerPort, router: Router, settings: Map<String, *>) :
        this (
            serverEngine,
            router,
            settings["serviceName"] as? String ?: DEFAULT_NAME,
            address(settings["bindAddress"] as? String ?: DEFAULT_ADDRESS),
            settings["bindPort"] as? Int ?: DEFAULT_PORT
        )

    val runtimePort
        get() = if (started()) serverEngine.runtimePort() else error("Server is not running")

    val portName: String = serverEngine.javaClass.simpleName

    fun started (): Boolean = serverEngine.started()

    fun run() {
        getRuntime().addShutdownHook(
            Thread (
                {
                    if (started ())
                        serverEngine.shutdown ()
                },
                "shutdown-${bindAddress.hostName}-$bindPort"
            )
        )

        serverEngine.startup (this)
        log.info { "$serverName started${createBanner()}" }
    }

    fun stop() {
        serverEngine.shutdown ()
        log.info { "$serverName stopped" }
    }

    private fun createBanner(): String {
        val heap = getMemoryMXBean().heapMemoryUsage
        val jvmMemory = "%,d".format(heap.init / 1024)
        val usedMemory = "%,d".format(heap.used / 1024)
        val bootTime = "%01.3f".format(getRuntimeMXBean().uptime / 1e3)
        val hostName = if (bindAddress.isAnyLocalAddress) ip else bindAddress.canonicalHostName

        val information = """
            SERVICE:     $serverName
            SERVER TYPE: $portName

            Running in '$hostname' with $cpuCount CPUs $jvmMemory KB
            Java $version [$name]
            Locale $locale Timezone $timezone Charset $charset

            Started in $bootTime s using $usedMemory KB
            Served at http://$hostName:$runtimePort
        """

        // TODO Load banner from ${serverName}.txt
        // TODO Do not trim the banner (it could break ASCII art ;)
        val bannerResource = serverName.toLowerCase().replace(' ', '_')
        val banner = (Resource("$bannerResource.txt").readText() ?: "") + information
        return banner
            .trimIndent()
            .lines()
            .joinToString(eol, eol + eol, eol) { " ".repeat(4) + it.trim() }
    }
}
