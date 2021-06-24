package com.hexagonkt.helpers

import java.lang.IllegalStateException
import java.lang.management.ManagementFactory
import java.lang.management.MemoryUsage
import java.net.InetAddress
import java.nio.charset.Charset
import java.util.*

import kotlin.reflect.KClass

/**
 * Object with utilities to gather information about the running JVM.
 *
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * TODO Add JVM exception handler to add information on known exceptions. I.e: Classpath handler not
 *   registered with information on how to fix it (call `ClasspathHandler.registerHandler()`)
 */
object Jvm {
    /** Default timezone. TODO Defining this lazily fails in macOS */
    val timeZone: TimeZone = TimeZone.getDefault()

    /** Default character set. */
    val charset: Charset by lazy { Charset.defaultCharset() }

    val locale: Locale by lazy { Locale.getDefault() }

    /** The hostname of the machine running this program. */
    val hostname: String by lazy { InetAddress.getLocalHost().hostName }

    /** The IP address of the machine running this program. */
    val ip: String by lazy { InetAddress.getLocalHost().hostAddress }

    val id: String by lazy { safeJmx { ManagementFactory.getRuntimeMXBean().name } }
    val name: String by lazy { safeJmx { ManagementFactory.getRuntimeMXBean().vmName } }
    val version: String by lazy { safeJmx { ManagementFactory.getRuntimeMXBean().specVersion } }
    val cpuCount: Int by lazy { Runtime.getRuntime().availableProcessors() }
    val timezone: String by lazy { System.getProperty("user.timezone") }
    val localeCode: String by lazy {
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

    @Suppress("UNCHECKED_CAST") // All allowed types are checked at runtime
    fun <T: Any> systemSetting(type: KClass<T>, name: String): T? =
        systemSettingRaw(name)?.let {
            when (type) {
                Boolean::class -> it.toBooleanStrictOrNull()
                Int::class -> it.toIntOrNull()
                Long::class -> it.toLongOrNull()
                Float::class -> it.toFloatOrNull()
                Double::class -> it.toDoubleOrNull()
                String::class -> it
                else -> error("Setting: '$name' has unsupported type: ${type.qualifiedName}")
            }
        } as? T

    inline fun <reified T: Any> systemSetting(name: String): T? =
        systemSetting(T::class, name)

    internal fun systemSettingRaw(name: String): String? =
        System.getProperty(name) ?: System.getenv(name)

    internal fun safeJmx(block: () -> String): String =
        try {
            if (System.getProperty(NO_JMX_PROPERTY) == null) block()
            else "N/A"
        }
        catch (e: Exception) {
            throw IllegalStateException(NO_JMX_ERROR, e)
        }

    // TODO Add command line parsing 'Options' and 'Commands' (maybe in its own package: `cli`)
}
