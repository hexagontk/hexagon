package com.hexagontk.core

import com.hexagontk.core.text.parseOrNull
import java.io.Console
import java.net.InetAddress
import java.nio.charset.Charset
import java.time.ZoneId
import java.util.*

import kotlin.reflect.KClass

/**
 * Object with utilities to gather information about the running platform.
 */
object Platform {
    private val systemSettingPattern: Regex by lazy { Regex("[_A-Za-z]+[_A-Za-z0-9]*") }

    /** Operating system name ('os.name' property). If `null` throws an exception. */
    val os: String by lazy { os() }

    /** Operating system type. */
    val osKind: OsKind by lazy { osKind() }

    /**
     * JVM Console, if the program don't have a console (i.e.: input or output redirected), an
     * exception is thrown.
     */
    val console: Console by lazy {
        System.console() ?: error("Program doesn't have a console (I/O may be redirected)")
    }

    /** True if the program has a console (terminal, TTY, PTY...), false if I/O is piped. */
    val isConsole: Boolean by lazy { System.console() != null }

    /** Current JVM runtime. */
    val runtime: Runtime by lazy { Runtime.getRuntime() }

    /** Default timezone. */
    val timeZone: TimeZone by lazy { TimeZone.getDefault() }

    /** Default zone ID. */
    val zoneId: ZoneId by lazy { timeZone.toZoneId() }

    /** Default character set. */
    val charset: Charset by lazy { Charset.defaultCharset() }

    /** Default locale for this instance of the Java Virtual Machine. */
    val locale: Locale by lazy { Locale.getDefault() }

    /** The host name of the machine running this program. */
    val hostName: String by lazy { InetAddress.getLocalHost().hostName }

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
     * Add a map to system properties, overriding entries if already set.
     *
     * @param settings Data to be added to system properties.
     */
    fun loadSystemSettings(settings: Map<String, String>) {
        settings.entries.forEach { (k, v) ->
            val matchPattern = k.matches(systemSettingPattern)
            check(matchPattern) { "Property name must match $systemSettingPattern ($k)" }
            System.setProperty(k, v)
        }
    }

    /**
     * Retrieve a setting by name by looking in OS environment variables first and in the JVM system
     * properties if not found.
     *
     * @param type Type of the requested parameter. Supported types are: boolean, int, long, float,
     *   double and string, throw an error if other type is supplied.
     * @param name Name of the searched parameter, can not be blank.
     * @return Value of the searched parameter in the requested type, `null` if the parameter is not
     *   found on the OS environment variables or in JVM system properties.
     */
    fun <T: Any> systemSettingOrNull(type: KClass<T>, name: String): T? =
        systemSettingRaw(name).parseOrNull(type)

    fun <T: Any> systemSetting(type: KClass<T>, name: String): T =
        systemSettingOrNull(type, name)
            ?: error("Required '${type.simpleName}' system setting '$name' not found")

    fun <T: Any> systemSetting(type: KClass<T>, name: String, defaultValue: T): T =
        systemSettingOrNull(type, name) ?: defaultValue

    /**
     * Retrieve a flag (boolean parameter) by name by looking in OS environment variables first and
     * in the JVM system properties if not found.
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
     *   found on the OS environment variables or in JVM system properties.
     */
    inline fun <reified T: Any> systemSettingOrNull(name: String): T? =
        systemSettingOrNull(T::class, name)

    inline fun <reified T: Any> systemSetting(name: String): T =
        systemSetting(T::class, name)

    inline fun <reified T: Any> systemSetting(name: String, defaultValue: T): T =
        systemSetting(T::class, name, defaultValue)

    private fun systemSettingRaw(name: String): String? {
        val correctName = name.matches(systemSettingPattern)
        require(correctName) { "Setting name must match $systemSettingPattern" }
        return System.getenv(name) ?: System.getenv(name.uppercase()) ?: System.getProperty(name)
    }

    /** Operating system name ('os.name' property). If `null` throws an exception. */
    internal fun os(): String =
        System.getProperty("os.name") ?: error("OS property ('os.name') not found")

    /** Operating system type. */
    internal fun osKind(): OsKind =
        os().lowercase().let {
            when {
                it.contains("win") -> OsKind.WINDOWS
                it.contains("mac") -> OsKind.MACOS
                it.contains("nux") -> OsKind.LINUX
                it.contains("nix") || it.contains("aix") -> OsKind.UNIX
                else -> error("Unsupported OS: ${os()}")
            }
        }
}
