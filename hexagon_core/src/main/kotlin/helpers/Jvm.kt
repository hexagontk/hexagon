package com.hexagonkt.helpers

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
    /** Default character set. */
    val charset: Charset by lazy { Charset.defaultCharset() }

    /** Default timezone. */
    val timeZone: TimeZone = TimeZone.getDefault()

    /** The hostname of the machine running this program. */
    val hostname: String = InetAddress.getLocalHost().hostName
    /** The IP address of the machine running this program. */
    val ip: String = InetAddress.getLocalHost().hostAddress

    val id: String = safeJmx { ManagementFactory.getRuntimeMXBean().name }
    val name: String = safeJmx { ManagementFactory.getRuntimeMXBean().vmName }
    val version: String = safeJmx { ManagementFactory.getRuntimeMXBean().specVersion }
    val cpuCount: Int = Runtime.getRuntime().availableProcessors()
    val timezone: String = System.getProperty("user.timezone")
    val locale: String = "%s_%s.%s".format(
        System.getProperty("user.language"),
        System.getProperty("user.country"),
        System.getProperty("file.encoding")
    )

    private val heap: MemoryUsage by lazy { ManagementFactory.getMemoryMXBean().heapMemoryUsage }

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
        if (System.getProperty("com.hexagonkt.noJmx") == null) block() else "N/A"
}
