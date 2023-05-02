package com.hexagonkt.core

import java.net.InetAddress
import java.nio.charset.Charset
import java.time.ZoneId
import java.util.*

import kotlin.reflect.KClass

/**
 * Object with utilities to gather information about the running JVM.
 */
object Jvm {
    private val runtime: Runtime by lazy { Runtime.getRuntime() }

    /** Default timezone. */
    val timeZone: TimeZone by lazy { TimeZone.getDefault() }

    /** Default zone ID. */
    val zoneId: ZoneId by lazy { timeZone.toZoneId() }

    /** Default character set. */
    val charset: Charset by lazy { Charset.defaultCharset() }

    /** Default locale for this instance of the Java Virtual Machine. */
    val locale: Locale by lazy { Locale.getDefault() }

    /** The hostname of the machine running this program. */
    val hostname: String by lazy { InetAddress.getLocalHost().hostName }

    /** The IP address of the machine running this program. */
    val ip: String by lazy { InetAddress.getLocalHost().hostAddress }

    /** Name of the JVM running this program. For example: OpenJDK 64-Bit Server VM. */
    val name: String by lazy { System.getProperty("java.vm.name", "N/A") }

    /** Java version aka language level. For example: 11 */
    val version: String by lazy { System.getProperty("java.vm.specification.version", "N/A") }

    /** Number of processors available to the Java virtual machine. */
    val cpuCount: Int by lazy { runtime.availableProcessors() }

    /** User locale consist of 2-letter language code, 2-letter country code and file encoding. */
    val localeCode: String by lazy {
        "%s_%s.%s".format(locale.language, locale.country, charset.name())
    }

    /**
     * Amount of memory in kilobytes available to the JVM.
     *
     * @return Total amount of memory in kilobytes.
     */
    fun totalMemory(): String =
        runtime.totalMemory().let { "%,d".format(it / 1024) }

    /**
     * Amount of used memory in kilobytes.
     *
     * @return Used memory in kilobytes.
     */
    fun usedMemory(): String =
        (runtime.totalMemory() - runtime.freeMemory()).let { "%,d".format(it / 1024) }

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
        systemSettingRaw(name).let { it.parseOrNull(type) }

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

    private fun systemSettingRaw(name: String): String? {
        require(name.isNotBlank()) { "Setting name can not be blank" }
        return System.getProperty(name, System.getenv(name))
    }
}
