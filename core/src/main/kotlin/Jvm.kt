package com.hexagonkt.core

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

    /** Default locale for this instance of the Java Virtual Machine. */
    val locale: Locale by lazy { Locale.getDefault() }

    /** The hostname of the machine running this program. */
    val hostname: String by lazy { InetAddress.getLocalHost().hostName }

    /** The IP address of the machine running this program. */
    val ip: String by lazy { InetAddress.getLocalHost().hostAddress }

    /** ID representing the running Java virtual machine */
    val id: String by lazy { safeJmx { ManagementFactory.getRuntimeMXBean().name } }

    /** Name of the JVM running this program. For example: OpenJDK 64-Bit Server VM. */
    val name: String by lazy { safeJmx { ManagementFactory.getRuntimeMXBean().vmName } }

    /** Java version aka language level. For example: 11 */
    val version: String by lazy { safeJmx { ManagementFactory.getRuntimeMXBean().specVersion } }

    /** Number of processors available to the Java virtual machine. */
    val cpuCount: Int by lazy { Runtime.getRuntime().availableProcessors() }

    /** User Time Zone property. Can be set with -Duser.timezone JVM argument. */
    val timezone: String by lazy { System.getProperty("user.timezone") }

    /** User locale consist of 2-letter language code, 2-letter country code and file encoding. */
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

    internal fun safeJmx(block: () -> String): String =
        try {
            if (System.getProperty(NO_JMX_PROPERTY) == null) block()
            else "N/A"
        }
        catch (e: Exception) {
            throw IllegalStateException(NO_JMX_ERROR, e)
        }

    fun systemFlag(name: String): Boolean =
        systemSetting(Boolean::class, name) ?: false

    inline fun <reified T: Any> systemSetting(name: String): T? =
        systemSetting(T::class, name)

    private fun systemSettingRaw(name: String): String? =
        System.getProperty(name) ?: System.getenv(name)
}
