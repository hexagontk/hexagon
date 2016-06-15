package co.there4.hexagon.web

import co.there4.hexagon.configuration.ConfigManager.serviceName
import co.there4.hexagon.configuration.ConfigManager
import co.there4.hexagon.util.*
import java.net.InetAddress
import java.lang.System.*
import java.lang.Runtime.*
import java.lang.management.ManagementFactory.*

abstract class Server (
    val bindAddress: InetAddress = InetAddress.getLocalHost(),
    val bindPort: Int = 4321) : Router() {

    companion object : CompanionLogger (Server::class)


    abstract fun started (): Boolean

    /**
     * Builds a server of a certain backend from a server definition and runs it.
     */
    protected abstract fun startup()

    /**
     * Stops the instance of the backend.
     */
    protected abstract fun shutdown()

    fun run() {
        getRuntime().addShutdownHook(
            Thread (
                {
                    if (started ())
                        shutdown ()
                },
                "shutdown-${bindAddress.hostName}-$bindPort"
            )
        )

        startup ()
        info ("$serviceName started${createBanner()}")
    }

    fun stop() {
        shutdown ()
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

        val serviceName = ConfigManager.serviceName
        val serviceDir = ConfigManager.serviceDir
        val servicePackage = ConfigManager.servicePackage
        val environment = (ConfigManager.environment ?: "N/A")
        val applicationLocale = locale
        val applicationTimezone = getProperty("user.timezone")
        val applicationCpus = runtime.availableProcessors()
        val applicationJvmMemory = String.format("%,d", heap.init / 1024)
        val applicationJvm = getRuntimeMXBean().vmName
        val applicationJvmVersion = getRuntimeMXBean().specVersion
        val applicationBootTime = String.format("%01.3f", bootTime / 1000f)
        val applicationUsedMemory = String.format("%,d", heap.used / 1024)
        val applicationHost = hostname

        val information = """
            SERVICE:     $serviceName
            PACKAGE:     $servicePackage
            DIRECTORY:   $serviceDir
            ENVIRONMENT: $environment

            Running in '$applicationHost' with $applicationCpus CPUs $applicationJvmMemory KB
            Java $applicationJvmVersion [$applicationJvm]
            Locale $applicationLocale Timezone $applicationTimezone

            Started in $applicationBootTime s using $applicationUsedMemory KB
        """

        val banner = EOL + EOL + (read ("banner.txt") ?: "") + information
            .replaceIndent(" ".repeat(4)).lines()
            .map { if (it.isBlank()) it.trim() else it }
            .joinToString(EOL) + EOL

        return banner
    }
}
