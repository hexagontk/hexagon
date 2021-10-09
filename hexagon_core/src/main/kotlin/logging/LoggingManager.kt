package com.hexagonkt.logging

import com.hexagonkt.helpers.fail
import com.hexagonkt.logging.jul.JulLoggingAdapter
import kotlin.reflect.KClass

/**
 * Manages Logs using [JulLoggingAdapter]
 */
object LoggingManager {
    var adapter: LoggingPort = JulLoggingAdapter

    /**
     * Set a logger logging level by name.
     *
     * @param [name] Logger name.
     * @param [level] One of the logging levels identifiers, e.g., TRACE
     * @see [LoggingLevel]
     */
    fun setLoggerLevel(name: String, level: LoggingLevel) {
        adapter.setLoggerLevel(name, level)
    }

    /**
     * Set a logging level for a logger with a class instance.
     *
     * @param [instance] class instance.
     * @param [level] One of the logging levels identifiers, e.g., TRACE
     * @see [LoggingLevel]
     */
    fun setLoggerLevel(instance: Any, level: LoggingLevel) {
        setLoggerLevel(instance::class, level)
    }

    /**
     * Set a logging level for a logger with a class name.
     *
     * @param [type] Class type.
     * @param [level] One of the logging levels identifiers, e.g., TRACE
     * @see [LoggingLevel]
     */
    fun setLoggerLevel(type: KClass<*>, level: LoggingLevel) {
        setLoggerLevel(type.qualifiedName ?: fail, level)
    }

    /**
     * Set a logger logging level for a logger with a default name.
     *
     * @param [level] One of the logging levels identifiers, e.g., TRACE
     * @see [LoggingLevel]
     */
    fun setLoggerLevel(level: LoggingLevel) {
        setLoggerLevel("", level)
    }
}
