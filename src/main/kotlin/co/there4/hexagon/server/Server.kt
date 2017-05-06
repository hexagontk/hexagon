package co.there4.hexagon.server

import co.there4.hexagon.helpers.*
import co.there4.hexagon.server.engine.ServerEngine
import co.there4.hexagon.settings.SettingsManager.environment
import co.there4.hexagon.settings.SettingsManager.setting

import java.lang.Runtime.getRuntime
import java.lang.management.ManagementFactory.getMemoryMXBean
import java.lang.management.ManagementFactory.getRuntimeMXBean
import java.net.InetAddress
import java.net.InetAddress.getByName as address

class Server (
    private val serverBackend: ServerEngine,
    val serviceName: String = setting("serviceName") ?: "Service",
    val bindAddress: InetAddress = address(setting("bindAddress") ?: "127.0.0.1") ?: err,
    val bindPort: Int = setting<Int>("bindPort") ?: 2010,
    val router: Router = Router()) {

    companion object : CachedLogger(Server::class)

    val runtimePort
        get() = if (started()) serverBackend.runtimePort() else error("Server is not running")

    fun started (): Boolean = serverBackend.started()

    fun run() {
        getRuntime().addShutdownHook(
            Thread (
                {
                    if (started ())
                        serverBackend.shutdown ()
                },
                "shutdown-${bindAddress.hostName}-$bindPort"
            )
        )

        serverBackend.startup (this)
        info ("$serviceName started${createBanner()}")
    }

    fun stop() {
        serverBackend.shutdown ()
        info ("$serviceName stopped")
    }

    private fun createBanner(): String {
        val heap = getMemoryMXBean().heapMemoryUsage
        val environment = environment ?: "N/A"
        val jvmMemory = "%,d".format(heap.init / 1024)
        val usedMemory = "%,d".format(heap.used / 1024)
        val bootTime = "%01.3f".format(getRuntimeMXBean().uptime / 1e3)

        val information = """
            SERVICE:     $serviceName
            ENVIRONMENT: $environment

            Running in '$hostname' with $cpuCount CPUs $jvmMemory KB
            Java $jvmVersion [$jvmName]
            Locale $locale Timezone $timezone

            Started in $bootTime s using $usedMemory KB
            Served at http://${bindAddress.canonicalHostName}:$runtimePort
        """

        val banner = EOL + EOL + (readResource("banner.txt") ?: "") + information
            .replaceIndent(" ".repeat(4)).lines()
            .map { if (it.isBlank()) it.trim() else it }
            .joinToString(EOL) + EOL

        return banner
    }
}
