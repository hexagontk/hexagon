package com.hexagonkt.core

import java.lang.management.ManagementFactory.getMemoryMXBean
import java.lang.management.ManagementFactory.getRuntimeMXBean
import java.lang.management.MemoryUsage
import java.lang.management.RuntimeMXBean
import java.net.InetAddress
import java.nio.charset.Charset
import java.util.*

import kotlin.reflect.KClass

/**
 * Object with utilities to gather information about the running JVM.
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

    /**
     * ID representing the running Java virtual machine. If the 'java.management' module is not
     * available returns `N/A`.
     */
    val id: String by lazy { runtime?.name ?: "N/A" }

    /** Name of the JVM running this program. For example: OpenJDK 64-Bit Server VM. */
    val name: String by lazy { System.getProperty("java.vm.name") }

    /** Java version aka language level. For example: 11 */
    val version: String by lazy { System.getProperty("java.vm.specification.version") }

    /** Number of processors available to the Java virtual machine. */
    val cpuCount: Int by lazy { Runtime.getRuntime().availableProcessors() }

    /** User Time Zone property. Can be set with -D user.timezone=<tz> JVM argument. */
    val timezone: String by lazy { System.getProperty("user.timezone") }

    /** User locale consist of 2-letter language code, 2-letter country code and file encoding. */
    val localeCode: String by lazy {
        "%s_%s.%s".format(locale.language, locale.country, charset.name())
    }

    /**
     * Amount of memory in kilobytes that the JVM initially requests from the operating system. If
     * the 'java.management' module is not available returns `N/A`.
     *
     * @return Initial amount of memory in kilobytes.
     */
    fun initialMemory(): String =
        heap?.let { "%,d".format(it.init / 1024) } ?: "N/A"

    /**
     * Amount of used memory in kilobytes. If the 'java.management' module is not available returns
     * `N/A`.
     *
     * @return Used memory in kilobytes.
     */
    fun usedMemory(): String =
        heap?.let { "%,d".format(it.used / 1024) } ?: "N/A"

    /**
     * Uptime of the Java virtual machine in seconds. If the 'java.management' module is not
     * available returns `N/A`.
     *
     * @return JVM uptime in seconds.
     */
    fun uptime(): String =
        runtime?.let { "%01.3f".format(it.uptime / 1e3) } ?: "N/A"

    /**
     * Retrieve a setting by name by looking in the JVM system properties first and in OS
     * environment variables if not found.
     *
     * @param type Type of the requested parameter. Supported types are: boolean, int, long, float,
     *   double and string, throw an error if other type is supplied.
     * @param name Name of the searched parameter, can not be blank.
     * @return Value of the searched parameter in the requested type, `null` if the parameter is not
     *   found on the JVM system properties and in OS environment variables.
     */
    fun <T: Any> systemSettingOrNull(type: KClass<T>, name: String): T? =
        systemSettingRaw(name).let { it.toOrNull(type) }

    fun <T: Any> systemSetting(type: KClass<T>, name: String): T =
        systemSettingOrNull(type, name)
            ?: error("Required '${type.simpleName}' system setting '$name' not found")

    /**
     * Retrieve a flag (boolean parameter) by name by looking in the JVM system properties first and
     * in OS environment variables if not found.
     *
     * @param name Name of the searched parameter, can not be blank.
     * @return True if the parameter is found and its value is exactly 'true', false otherwise.
     */
    fun systemFlag(name: String): Boolean =
        systemSettingOrNull(Boolean::class, name) ?: false

    /**
     * Utility method for retrieving a system setting, check [systemSettingOrNull] for details.
     *
     * @param T Type of the requested parameter. Supported types are: boolean, int, long, float,
     *   double and string, throw an error if other type is supplied.
     * @param name Name of the searched parameter, can not be blank.
     * @return Value of the searched parameter in the requested type, `null` if the parameter is not
     *   found on the JVM system properties and in OS environment variables.
     */
    inline fun <reified T: Any> systemSettingOrNull(name: String): T? =
        systemSettingOrNull(T::class, name)

    inline fun <reified T: Any> systemSetting(name: String): T =
        systemSetting(T::class, name)

    private val heap: MemoryUsage? by lazy {
        try { getMemoryMXBean().heapMemoryUsage } catch (_: NoClassDefFoundError) { null }
    }

    private val runtime: RuntimeMXBean? by lazy {
        try { getRuntimeMXBean() } catch (_: NoClassDefFoundError) { null }
    }

    private fun systemSettingRaw(name: String): String? {
        require(name.isNotBlank()) { "Setting name can not be blank" }
        return System.getProperty(name) ?: System.getenv(name)
    }
}
