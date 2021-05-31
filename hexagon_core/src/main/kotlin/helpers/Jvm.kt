package com.hexagonkt.helpers

import java.lang.IllegalStateException
import java.lang.management.ManagementFactory
import java.lang.management.MemoryUsage
import java.net.InetAddress
import java.nio.charset.Charset

import java.util.TimeZone

/**
 * Object with utilities to gather information about the running JVM.
 *
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 */
object Jvm {
    /** Default timezone. TODO Defining this lazily fails in macOS */
    val timeZone: TimeZone = TimeZone.getDefault()

    /** Default character set. */
    val charset: Charset by lazy { Charset.defaultCharset() }

    /** The hostname of the machine running this program. */
    val hostname: String by lazy { InetAddress.getLocalHost().hostName }

    /** The IP address of the machine running this program. */
    val ip: String by lazy { InetAddress.getLocalHost().hostAddress }

    val id: String by lazy { safeJmx { ManagementFactory.getRuntimeMXBean().name } }
    val name: String by lazy { safeJmx { ManagementFactory.getRuntimeMXBean().vmName } }
    val version: String by lazy { safeJmx { ManagementFactory.getRuntimeMXBean().specVersion } }
    val cpuCount: Int by lazy { Runtime.getRuntime().availableProcessors() }
    val timezone: String by lazy { System.getProperty("user.timezone") }
    val locale: String by lazy {
        "%s_%s.%s".format(
            System.getProperty("user.language"),
            System.getProperty("user.country"),
            System.getProperty("file.encoding")
        )
    }

    private val heap: MemoryUsage by lazy { ManagementFactory.getMemoryMXBean().heapMemoryUsage }

    private const val NO_JMX_PROPERTY = "com.hexagonkt.noJmx"
    internal const val NO_JMX_ERROR =
        "JMX Error. If JMX is not available, set the '$NO_JMX_PROPERTY' system property"

    fun initialMemory(): String =
        safeJmx { "%,d".format(heap.init / 1024) }

    fun usedMemory(): String =
        safeJmx { "%,d".format(heap.used / 1024) }

    fun uptime(): String =
        safeJmx { "%01.3f".format(ManagementFactory.getRuntimeMXBean().uptime / 1e3) }

    fun systemSetting(name: String): String? =
        System.getProperty(name) ?: System.getenv(name)

    fun systemSetting(name: String, default: String): String =
        systemSetting(name) ?: default

    internal fun safeJmx(block: () -> String): String =
        try {
            if (System.getProperty(NO_JMX_PROPERTY) == null) block()
            else "N/A"
        }
        catch (e: Exception) {
            throw IllegalStateException(NO_JMX_ERROR, e)
        }
}
