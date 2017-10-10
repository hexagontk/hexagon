package com.hexagonkt.server

import com.hexagonkt.helpers.*
import com.hexagonkt.settings.SettingsManager.environment

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
    private val serverEngine: ServerEngine,
    val serverName: String = Server.DEFAULT_NAME,
    val bindAddress: InetAddress = address(Server.DEFAULT_ADDRESS),
    val bindPort: Int = Server.DEFAULT_PORT,
    val router: Router = Router()) {

    internal companion object : CachedLogger(Server::class) {
        internal const val DEFAULT_NAME = "<undefined>"
        internal const val DEFAULT_ADDRESS = "127.0.0.1"
        internal const val DEFAULT_PORT = 2010
    }

    constructor(serverEngine: ServerEngine, settings: Map<String, *>, router: Router = Router()) :
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
        info ("$serverName started${createBanner()}")
    }

    fun stop() {
        serverEngine.shutdown ()
        info ("$serverName stopped")
    }

    private fun createBanner(): String {
        val heap = getMemoryMXBean().heapMemoryUsage
        val environment = environment ?: "N/A"
        val jvmMemory = "%,d".format(heap.init / 1024)
        val usedMemory = "%,d".format(heap.used / 1024)
        val bootTime = "%01.3f".format(getRuntimeMXBean().uptime / 1e3)

        val information = """
            SERVICE:     $serverName
            ENVIRONMENT: $environment

            Running in '$hostname' with $cpuCount CPUs $jvmMemory KB
            Java $jvmVersion [$jvmName]
            Locale $locale Timezone $timezone

            Started in $bootTime s using $usedMemory KB
            Served at http://${bindAddress.canonicalHostName}:$runtimePort
        """

        val banner = eol + eol + (readResource("banner.txt") ?: "") + information
            .replaceIndent(" ".repeat(4)).lines()
            .map { if (it.isBlank()) it.trim() else it }
            .joinToString(eol) + eol

        return banner
    }
}
