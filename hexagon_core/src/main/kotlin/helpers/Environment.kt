package com.hexagonkt.helpers

import java.lang.management.ManagementFactory
import java.lang.management.MemoryUsage
import java.net.InetAddress

/** The hostname of the machine running this program. */
val hostname: String = InetAddress.getLocalHost().hostName
/** The IP address of the machine running this program. */
val ip: String = InetAddress.getLocalHost().hostAddress

val jvmId: String = ManagementFactory.getRuntimeMXBean().name
val jvmName: String = ManagementFactory.getRuntimeMXBean().vmName
val jvmVersion: String = ManagementFactory.getRuntimeMXBean().specVersion
val cpuCount: Int = Runtime.getRuntime().availableProcessors()
val timezone: String = System.getProperty("user.timezone")
val locale: String = "%s_%s.%s".format(
    System.getProperty("user.language"),
    System.getProperty("user.country"),
    System.getProperty("file.encoding")
)

private val heap: MemoryUsage = ManagementFactory.getMemoryMXBean().heapMemoryUsage

fun jvmMemory(): String = "%,d".format(heap.init / 1024)
fun usedMemory(): String = "%,d".format(heap.used / 1024)
fun uptime(): String = "%01.3f".format(ManagementFactory.getRuntimeMXBean().uptime / 1e3)
