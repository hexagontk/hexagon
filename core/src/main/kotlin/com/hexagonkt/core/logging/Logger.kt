package com.hexagonkt.core.logging

import com.hexagonkt.core.text.stripAnsi
import java.lang.System.Logger.Level
import java.lang.System.Logger.Level.*
import kotlin.reflect.KClass

/**
 * Logger class with Kotlin usability improvements. It is backed by a [System.Logger] instance.
 *
 * @param name Logger name. It is shown in the logs messages and used for log filtering.
 * @sample com.hexagonkt.core.logging.LoggerTest.loggerUsage
 */
class Logger(
    val name: String,
    internal val logger: System.Logger = System.getLogger(name)
) {
    /**
     * Check if this logger is enabled for a given log level.
     *
     * @param level Level to check.
     * @return True if this logger is enabled for the supplied level.
     */
    fun isLoggable(level: Level): Boolean =
        logger.isLoggable(level)

    /**
     * Log a message, with associated exception information.
     *
     * @param level Level used in the log statement.
     * @param exception The exception associated with log message.
     * @param message The message supplier to use in the log statement.
     */
    fun <E : Throwable> log(level: Level, exception: E, message: (E) -> Any?) {
        val messageSupplier = { stripAnsi(message(exception), useColor) }
        logger.log(level, messageSupplier, exception)
    }

    /**
     * Log a message.
     *
     * @param level Level used in the log statement.
     * @param message The message supplier to use in the log statement.
     */
    fun log(level: Level, message: () -> Any?) {
        val messageSupplier = { stripAnsi(message(), useColor) }
        logger.log(level, messageSupplier)
    }

    /**
     * Logger class with Kotlin improvements like lazy evaluation.
     *
     * @param type Logger type. It is shown in the logs messages and used for log filtering.
     */
    constructor(type: KClass<*>):
        this(type.qualifiedName ?: error("Cannot get qualified name of type"))

    /**
     * Log a message using [TRACE] level.
     *
     * @param message The required message to log.
     */
    fun trace(message: () -> Any?) {
        logger.log(TRACE, message)
    }

    /**
     * Log a message using [DEBUG] level.
     *
     * @param message The required message to log.
     */
    fun debug(message: () -> Any?) {
        logger.log(DEBUG, message)
    }

    /**
     * Log a message using [INFO] level.
     *
     * @param message The required message to log.
     */
    fun info(message: () -> Any?) {
        logger.log(INFO, message)
    }

    /**
     * Log a message using [WARNING] level.
     *
     * @param message The required message to log.
     */
    fun warn(message: () -> Any?) {
        logger.log(WARNING, message)
    }

    /**
     * Log a message using [ERROR] level.
     *
     * @param message The required message to log.
     */
    fun error(message: () -> Any?) {
        logger.log(ERROR, message)
    }

    /**
     * Log a message using [WARNING] level with associated exception information.
     *
     * @param exception The exception associated with log message.
     * @param message The message to log (optional). If not supplied it will be empty.
     */
    fun <E : Throwable> warn(exception: E?, message: (E?) -> Any? = { "" }) {
        if (exception == null) log(WARNING) { message(null) }
        else log(WARNING, exception, message)
    }

    /**
     * Log a message using [ERROR] level with associated exception information.
     *
     * @param exception The exception associated with log message.
     * @param message The message to log (function to optional). If not supplied it will be empty.
     */
    fun <E : Throwable> error(exception: E?, message: (E?) -> Any? = { "" }) {
        if (exception == null) log(ERROR) { message(null) }
        else log(ERROR, exception, message)
    }

    internal fun <T> stripAnsi(receiver: T?, apply: Boolean): String? =
        receiver?.toString()?.let { if (apply) it.stripAnsi() else it }
}
