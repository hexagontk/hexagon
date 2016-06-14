package co.there4.hexagon.web

import co.there4.hexagon.configuration.ConfigManager
import co.there4.hexagon.util.*
import java.net.InetAddress
import java.lang.System.*
import java.lang.Runtime.*
import java.io.File
import java.lang.management.ManagementFactory.*
import java.util.*
import kotlin.reflect.KClass

abstract class Server (
    val bindAddress: InetAddress = InetAddress.getLocalHost(),
    val bindPort: Int = 4321,

    val keystore: String? = null,
    val keystorePassword: String? = null,
    val truststore: String? = null,
    val truststorePassword: String? = null) : Router() {

    companion object : CompanionLogger (Server::class) {
        val BASE_DIR = File (Server::class.java.protectionDomain.codeSource.location.toURI())
    }

    open val localPort: Int get() = bindPort

    val assets: MutableList<String> = ArrayList ()
    val errors: MutableMap<Class<out Exception>, Exchange.(e: Exception) -> Unit> = LinkedHashMap ()

    /** Name of the application. Used for logging and configuration. */
    private val packageName = javaClass.`package`.name
    val name =
        if (packageName.startsWith("co.there4.hexagon.http")) "Blacksheep"
        else javaClass.simpleName

    abstract fun started (): Boolean

    fun handleException (exception: Exception, exchange: Exchange) =
            exchange.(errors[exception.javaClass] ?: { throw exception })(exception)

    fun assets (path: String) = assets.add (path)

    fun error(exception: Class<out Exception>, callback: Exchange.(e: Exception) -> Unit) =
            errors.put (exception, callback)

    fun error(exception: KClass<out Exception>, callback: Exchange.(e: Exception) -> Unit) =
            error (exception.java, callback)

    fun notFound () {}
    fun internalError (callback: Exchange.(e: Exception) -> Unit) =
        error (Exception::class, callback)

    fun run() {
        getRuntime().addShutdownHook(
            Thread (
                {
                    if (started ())
                        shutdown ()
                    /*
                    try {
                        if (started)
                            stop ()
                    }
                    catch (e: Exception) {
                        // It could be trying to stop an already closed server (TODO FIX)
                    }
                    */
                },
                "shutdown-${bindAddress.hostName}-$bindPort"
            )
        )

        startup ()
        info ("$name started${createBanner()}")
    }

    fun stop() {
        shutdown ()
        info ("$name stopped")
    }

    /**
     * Builds a server of a certain backend from a server definition and runs it.
     */
    protected abstract fun startup()

    /**
     * Stops the instance of the backend.
     */
    protected abstract fun shutdown()

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
