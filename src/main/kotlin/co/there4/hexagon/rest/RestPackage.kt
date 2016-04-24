package co.there4.hexagon.rest

import co.there4.hexagon.repository.MongoIdRepository
import co.there4.hexagon.ratpack.KChain
import java.lang.Runtime.getRuntime
import java.lang.String.format
import java.lang.System.currentTimeMillis
import java.lang.System.getProperty
import java.lang.management.ManagementFactory.getMemoryMXBean
import java.lang.management.ManagementFactory.getRuntimeMXBean

import co.there4.hexagon.ratpack.KServerSpec
import co.there4.hexagon.util.CompanionLogger
import co.there4.hexagon.util.EOL
import co.there4.hexagon.util.filterVars
import co.there4.hexagon.util.read
import co.there4.hexagon.util.hostname
import ratpack.server.RatpackServer

/*
 * TODO Add base class for Application (setup locales, etc.) for web applications
 * TODO Add base class for Services  (the setup for applications is not needed here)
 */
object RestPackage : CompanionLogger (RestPackage::class)

val information = """
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

fun appStart(cb: KServerSpec.() -> Unit): RatpackServer {
    val server = RatpackServer.start { KServerSpec(it).(cb)() }

    // TODO Setup metrics
    showBanner ()

    return server
}

fun <T : Any, K : Any> KChain.crud (repository: MongoIdRepository<T, K>): KChain {
    RestCrud (repository, this)
    return this
}

