package co.there4.hexagon.web

import java.net.InetAddress.getByName as address
import co.there4.hexagon.util.err

import co.there4.hexagon.settings.SettingsManager
import co.there4.hexagon.settings.SettingsManager.setting
import co.there4.hexagon.util.*
import co.there4.hexagon.web.backend.IServer
import java.net.InetAddress
import java.lang.System.*
import java.lang.Runtime.*
import java.lang.management.ManagementFactory.*

class Server (
    private val serverBackend: IServer,
    val bindAddress: InetAddress = address(setting<String>("bindAddress") ?: "127.0.0.1") ?: err,
    val bindPort: Int = setting<Int>("bindPort") ?: 2010) : Router() {

    companion object : CachedLogger(Server::class)

    val serviceName by lazy { SettingsManager["serviceName"] ?: "Hexagon" }

    val runtimePort get() = serverBackend.runtimePort()

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
        val runtime = getRuntime()
        val heap = getMemoryMXBean().heapMemoryUsage
        val bootTime = currentTimeMillis() - getRuntimeMXBean().startTime

        val locale = "%s_%s.%s".format(
            getProperty("user.language"),
            getProperty("user.country"),
            getProperty("file.encoding")
        )

        val environment = (SettingsManager.environment ?: "N/A")
        val applicationLocale = locale
        val applicationTimezone = getProperty("user.timezone")
        val applicationCpus = runtime.availableProcessors()
        val applicationJvmMemory = String.format("%,d", heap.init / 1024)
        val applicationJvm = getRuntimeMXBean().vmName
        val applicationJvmVersion = getRuntimeMXBean().specVersion
        val applicationBootTime = String.format("%01.3f", bootTime / 1e3)
        val applicationUsedMemory = String.format("%,d", heap.used / 1024)
        val applicationHost = hostname

        val information = """
            SERVICE:     $serviceName
            ENVIRONMENT: $environment

            Running in '$applicationHost' with $applicationCpus CPUs $applicationJvmMemory KB
            Java $applicationJvmVersion [$applicationJvm]
            Locale $applicationLocale Timezone $applicationTimezone

            Started in $applicationBootTime s using $applicationUsedMemory KB
            Served at http://${bindAddress.canonicalHostName}:$runtimePort
        """

        val banner = EOL + EOL + (readResource("banner.txt") ?: "") + information
            .replaceIndent(" ".repeat(4)).lines()
            .map { if (it.isBlank()) it.trim() else it }
            .joinToString(EOL) + EOL

        return banner
    }
}
