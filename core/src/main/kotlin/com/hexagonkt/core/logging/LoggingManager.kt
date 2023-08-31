package com.hexagonkt.core.logging

import kotlin.reflect.KClass

/**
 * Manages Logs using [SystemLoggingAdapter]
 */
object LoggingManager {
    var useColor: Boolean = true
    var adapter: LoggingPort = SystemLoggingAdapter()
    var defaultLoggerName: String = "com.hexagonkt.core.logging"
        set(value) {
            require(value.isNotEmpty()) { "Default logger name cannot be empty string" }
            field = value
        }

    /**
     * Set a logger logging level by name.
     *
     * @param name Logger name.
     * @param level One of the logging levels identifiers, e.g., TRACE
     */
    fun setLoggerLevel(name: String, level: LoggingLevel) {
        adapter.setLoggerLevel(name, level)
    }

    /**
     * Set a logging level for a logger with a class name.
     *
     * @param type Class type.
     * @param level One of the logging levels identifiers, e.g., TRACE
     */
    fun setLoggerLevel(type: KClass<*>, level: LoggingLevel) {
        setLoggerLevel(qualifiedName(type), level)
    }

    /**
     * Set a logger logging level for a logger with a default name.
     *
     * @param level One of the logging levels identifiers, e.g., TRACE
     */
    fun setLoggerLevel(level: LoggingLevel) {
        setLoggerLevel("", level)
    }

    /**
     * Check if a logging level is enabled for a logger.
     *
     * @param name Logger name.
     * @param level One of the logging levels identifiers, e.g., TRACE
     * @return True if the supplied level is enabled for the passed logger name.
     */
    fun isLoggerLevelEnabled(name: String, level: LoggingLevel): Boolean =
        adapter.isLoggerLevelEnabled(name, level)

    /**
     * Check if a logging level is enabled for a logger with an instance.
     *
     * @param instance class instance.
     * @param level One of the logging levels identifiers, e.g., TRACE
     * @return True if the supplied level is enabled for the passed logger name.
     */
    fun isLoggerLevelEnabled(instance: Any, level: LoggingLevel): Boolean =
        isLoggerLevelEnabled(instance::class, level)

    /**
     * Check if a logging level is enabled for a logger with a class name.
     *
     * @param type Class type.
     * @param level One of the logging levels identifiers, e.g., TRACE
     * @return True if the supplied level is enabled for the passed logger name.
     */
    fun isLoggerLevelEnabled(type: KClass<*>, level: LoggingLevel): Boolean =
        isLoggerLevelEnabled(qualifiedName(type), level)

    /**
     * Check if a logging level is enabled for the root logger.
     *
     * @param level One of the logging levels identifiers, e.g., TRACE
     * @return True if the supplied level is enabled for the passed logger name.
     */
    fun isLoggerLevelEnabled(level: LoggingLevel): Boolean =
        isLoggerLevelEnabled("", level)

    private fun qualifiedName(type: KClass<*>): String =
        type.qualifiedName ?: error("Cannot get qualified name of type")
}
