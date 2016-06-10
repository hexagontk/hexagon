package co.there4.hexagon.web

import java.net.InetAddress
import java.lang.System.*
import java.lang.Runtime.*
import co.there4.hexagon.util.CompanionLogger
import co.there4.hexagon.util.filter
import java.io.File
import java.lang.Thread.currentThread
import java.lang.management.ManagementFactory.*
import java.net.UnknownHostException
import java.util.*
import kotlin.reflect.KClass

abstract class Server (
    val bind: InetAddress = InetAddress.getLocalHost(),
    val port: Int = 4321,

    val keystore: String? = null,
    val keystorePassword: String? = null,
    val truststore: String? = null,
    val truststorePassword: String? = null) : Router() {

    companion object : CompanionLogger (Server::class) {
        val BASE_DIR = File (Server::class.java.protectionDomain.codeSource.location.toURI())
    }

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
                "shutdown-${bind.hostName}-$port"
            )
        )

        startup ()
        info ("$name started${createBanner()}")
    }

    private fun createBanner(): String {
        val bannerResource = currentThread().contextClassLoader.getResourceAsStream("banner.txt")
        val bannerTemplate = bannerResource?.reader()?.readText() ?: ""

        val rt = getRuntime ()
        val heap = getMemoryMXBean ().heapMemoryUsage
        val bootTime = currentTimeMillis () - getRuntimeMXBean ().startTime
        val host =
            try { InetAddress.getLocalHost ().canonicalHostName }
            catch (e: UnknownHostException) { "UNKNOWN" }

        return bannerTemplate.filter("\${", "}",
            Pair ("blacksheep.backend", javaClass.simpleName),
            Pair ("blacksheep.bind", bind.hostName),
            Pair ("blacksheep.port", port.toString ()),
            Pair ("blacksheep.keystore.file", keystore ?: ""),
            Pair ("blacksheep.truststore.file", truststore ?: ""),
            Pair ("blacksheep.host", host),
            Pair ("blacksheep.cpus", rt.availableProcessors ().toString ()),
            Pair ("blacksheep.jvm.memory", "%,d".format (heap.init shr 10)),
            Pair ("blacksheep.jvm", getRuntimeMXBean ().vmName),
            Pair ("blacksheep.jvm.version", getRuntimeMXBean ().specVersion),
            Pair ("blacksheep.boot.time", "%01.3f".format (bootTime / 1000f)),
            Pair ("blacksheep.used.memory", "%,d".format (heap.used shr 10))
        )
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
}
