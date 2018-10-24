package com.hexagonkt.server

import com.hexagonkt.helpers.*
import com.hexagonkt.helpers.Environment.cpuCount
import com.hexagonkt.helpers.Environment.hostname
import com.hexagonkt.helpers.Environment.ip
import com.hexagonkt.helpers.Environment.jvmName
import com.hexagonkt.helpers.Environment.jvmVersion
import com.hexagonkt.helpers.Environment.locale
import com.hexagonkt.helpers.Environment.timezone
import com.hexagonkt.settings.SettingsManager.environment
import org.slf4j.Logger

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
    /** Engine used to run this HTTP server. */
    private val serverEngine: ServerPort,
    val serverName: String = Server.DEFAULT_NAME,
    val bindAddress: InetAddress = address(Server.DEFAULT_ADDRESS),
    val bindPort: Int = Server.DEFAULT_PORT,
    val router: Router = Router()) {

    internal companion object {
        internal const val DEFAULT_NAME = "<undefined>"
        internal const val DEFAULT_ADDRESS = "127.0.0.1"
        internal const val DEFAULT_PORT = 2010
    }

    private val log: Logger = logger()

    constructor(serverEngine: ServerPort, settings: Map<String, *>, router: Router = Router()) :
        this (
            serverEngine,
            settings["serviceName"] as? String ?: DEFAULT_NAME,
            address(settings["bindAddress"] as? String ?: DEFAULT_ADDRESS),
            settings["bindPort"] as? Int ?: DEFAULT_PORT,
            router
        )

    val runtimePort
        get() = if (started()) serverEngine.runtimePort() else error("Server is not running")

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
        log.info ("$serverName started${createBanner()}")
    }

    fun stop() {
        serverEngine.shutdown ()
        log.info ("$serverName stopped")
    }

    private fun createBanner(): String {
        val heap = getMemoryMXBean().heapMemoryUsage
        val jvmMemory = "%,d".format(heap.init / 1024)
        val usedMemory = "%,d".format(heap.used / 1024)
        val bootTime = "%01.3f".format(getRuntimeMXBean().uptime / 1e3)
        val hostName = if (bindAddress.isAnyLocalAddress) ip else bindAddress.canonicalHostName

        // TODO Handle environment not found (when Settings is finished)
        val information = """
            SERVICE:     $serverName
            ENVIRONMENT: $environment

            Running in '$hostname' with $cpuCount CPUs $jvmMemory KB
            Java $jvmVersion [$jvmName]
            Locale $locale Timezone $timezone

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
