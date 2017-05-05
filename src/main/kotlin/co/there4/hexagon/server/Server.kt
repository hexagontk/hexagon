package co.there4.hexagon.server

import java.net.InetAddress.getByName as address
import co.there4.hexagon.helpers.err

import co.there4.hexagon.settings.SettingsManager
import co.there4.hexagon.settings.SettingsManager.setting
import co.there4.hexagon.helpers.*
import co.there4.hexagon.server.engine.ServerEngine
import java.net.InetAddress
import java.lang.System.*
import java.lang.Runtime.*
import java.lang.management.ManagementFactory.*

class Server (
    private val serverBackend: ServerEngine,
    val bindAddress: InetAddress = address(setting<String>("bindAddress") ?: "127.0.0.1") ?: err,
    val bindPort: Int = setting<Int>("bindPort") ?: 2010,
    val router: Router = Router()) {

    companion object : CachedLogger(Server::class)

    val serviceName by lazy { SettingsManager["serviceName"] ?: "Hexagon" }
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
        val locale = "%s_%s.%s".format(
            getProperty("user.language"),
            getProperty("user.country"),
            getProperty("file.encoding")
        )

        val environment = (SettingsManager.environment ?: "N/A")
        val timezone = getProperty("user.timezone")
        val cpuCount = getRuntime().availableProcessors()
        val jvmName = getRuntimeMXBean().vmName
        val jvmVersion = getRuntimeMXBean().specVersion
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
