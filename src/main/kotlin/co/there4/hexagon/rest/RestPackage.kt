package co.there4.hexagon.rest

import co.there4.hexagon.configuration.ConfigManager
import co.there4.hexagon.repository.MongoIdRepository
import java.lang.Runtime.getRuntime
import java.lang.String.format
import java.lang.System.currentTimeMillis
import java.lang.System.getProperty
import java.lang.management.ManagementFactory.getMemoryMXBean
import java.lang.management.ManagementFactory.getRuntimeMXBean
import java.net.InetAddress.getByName as address

import co.there4.hexagon.util.*
import co.there4.hexagon.web.Server
import co.there4.hexagon.web.blacksheep
import co.there4.hexagon.web.jetty.JettyServer

/*
 * TODO Add base class for Application (setup locales, etc.) for web applications
 * TODO Add base class for Services (the setup for applications is not needed here)
 */
object RestPackage : CompanionLogger (RestPackage::class)

val information = """
    SERVICE:     #{service.name}
    PACKAGE:     #{service.package}
    DIRECTORY:   #{service.dir}
    ENVIRONMENT: #{environment}

    Running in '#{application.host}' with #{application.cpus} CPUs #{application.jvm.memory} KB
    Java #{application.jvm.version} [#{application.jvm}]
    Locale #{application.locale} Timezone #{application.timezone}

    Started in #{application.boot.time} s using #{application.used.memory} KB
    """

private fun showBanner() {
    val runtime = getRuntime()
    val heap = getMemoryMXBean().getHeapMemoryUsage()
    val bootTime = currentTimeMillis() - getRuntimeMXBean().getStartTime()

    val locale = "%s_%s.%s".format(
        getProperty("user.language"),
        getProperty("user.country"),
        getProperty("file.encoding")
    )

    val variables = mapOf (
        "service.name" to ConfigManager.serviceName,
        "service.dir" to ConfigManager.serviceDir,
        "service.package" to ConfigManager.servicePackage,
        "environment" to (ConfigManager.environment ?: "N/A"),
        "application.locale" to locale,
        "application.timezone" to getProperty("user.timezone"),
        "application.cpus" to runtime.availableProcessors(),
        "application.jvm.memory" to format("%,d", heap.getInit() / 1024),
        "application.jvm" to getRuntimeMXBean().getVmName(),
        "application.jvm.version" to getRuntimeMXBean().getSpecVersion(),
        "application.boot.time" to format("%01.3f", bootTime / 1000f),
        "application.used.memory" to format("%,d", heap.getUsed() / 1024),
        "application.host" to hostname
    )

    val banner = EOL + EOL + (read ("banner.txt") ?: "") + information
        .replaceIndent(" ".repeat(4)).lines()
        .map { if (it.isBlank()) it.trim() else it }
        .joinToString(EOL) + EOL

    RestPackage.info(banner.filterVars(variables))
}

fun applicationStart(cb: Server.() -> Unit): Server {
    val bindPort = ConfigManager["bindPort"] as Int? ?: 5050
    val bindAddress = address(ConfigManager["bindAddress"] as String? ?: "127.0.0.1")

    val server = JettyServer (
        bindPort = bindPort,
        bindAddress = bindAddress
    )

    server.(cb)()
    server.run()

    // TODO Setup metrics
    showBanner ()

    return server
}

fun <T : Any, K : Any> Server.crud (repository: MongoIdRepository<T, K>) {
    RestCrud (repository, this)
}

fun <T : Any, K : Any> crud (repository: MongoIdRepository<T, K>) {
    blacksheep.crud (repository)
}

// TODO Add initialization for applications and services
//class RestService
//class RestApplication
