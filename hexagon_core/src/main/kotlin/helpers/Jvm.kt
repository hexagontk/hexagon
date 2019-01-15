package com.hexagonkt.helpers

import java.lang.management.ManagementFactory
import java.lang.management.MemoryUsage
import java.net.InetAddress
import java.nio.charset.Charset

import java.util.TimeZone

object Jvm {
    /** Default character set. */
    val charset: Charset by lazy { Charset.defaultCharset() }

    /** Default timezone. */
    val timeZone: TimeZone = TimeZone.getDefault()

    /** The hostname of the machine running this program. */
    val hostname: String = InetAddress.getLocalHost().hostName
    /** The IP address of the machine running this program. */
    val ip: String = InetAddress.getLocalHost().hostAddress

    val id: String = ManagementFactory.getRuntimeMXBean().name
    val name: String = ManagementFactory.getRuntimeMXBean().vmName
    val version: String = ManagementFactory.getRuntimeMXBean().specVersion
    val cpuCount: Int = Runtime.getRuntime().availableProcessors()
    val timezone: String = System.getProperty("user.timezone")
    val locale: String = "%s_%s.%s".format(
        System.getProperty("user.language"),
        System.getProperty("user.country"),
        System.getProperty("file.encoding")
    )

    private val heap: MemoryUsage = ManagementFactory.getMemoryMXBean().heapMemoryUsage

    fun initialMemory(): String = "%,d".format(heap.init / 1024)

    fun usedMemory(): String = "%,d".format(heap.used / 1024)

    fun uptime(): String = "%01.3f".format(ManagementFactory.getRuntimeMXBean().uptime / 1e3)

    fun systemSetting(name: String): String? = System.getProperty(name) ?: System.getenv(name)

    fun systemSetting(name: String, default: String): String = systemSetting(name) ?: default
}
