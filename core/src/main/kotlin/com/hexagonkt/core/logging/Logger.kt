package com.hexagonkt.core.logging

import kotlin.reflect.KClass
import com.hexagonkt.core.logging.LoggingLevel.*

/**
 * Logger class with Kotlin improvements like lazy evaluation. It is backed by a logging port.
 *
 * @param name Logger name. It is shown in the logs messages and used for log filtering.
 * @sample com.hexagonkt.core.HexagonCoreSamplesTest.loggerUsage
 */
class Logger(val name: String) {

    internal val log: LoggerPort = LoggingManager.adapter.createLogger(name)

    /**
     * Log a message, with associated exception information.
     *
     * @see LoggerPort.log
     */
    fun <E : Throwable> log(level: LoggingLevel, exception: E, message: (E) -> Any?) {
        log.log(level, exception, message)
    }

    /**
     * Log a message.
     *
     * @see LoggerPort.log
     */
    fun log(level: LoggingLevel, message: () -> Any?) {
        log.log(level, message)
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
        log.log(TRACE, message)
    }

    /**
     * Log a message using [DEBUG] level.
     *
     * @param message The required message to log.
     */
    fun debug(message: () -> Any?) {
        log.log(DEBUG, message)
    }

    /**
     * Log a message using [INFO] level.
     *
     * @param message The required message to log.
     */
    fun info(message: () -> Any?) {
        log.log(INFO, message)
    }

    /**
     * Log a message using [WARN] level.
     *
     * @param message The required message to log.
     */
    fun warn(message: () -> Any?) {
        log.log(WARN, message)
    }

    /**
     * Log a message using [ERROR] level.
     *
     * @param message The required message to log.
     */
    fun error(message: () -> Any?) {
        log.log(ERROR, message)
    }

    /**
     * Log a message using [WARN] level with associated exception information.
     *
     * @param exception The exception associated with log message.
     * @param message The message to log (optional). If not supplied it will be empty.
     */
    fun <E : Throwable> warn(exception: E?, message: (E?) -> Any? = { "" }) {
        if (exception == null) log.log(WARN) { message(null) }
        else log.log(WARN, exception, message)
    }

    /**
     * Log a message using [ERROR] level with associated exception information.
     *
     * @param exception The exception associated with log message.
     * @param message The message to log (optional). If not supplied it will be empty.
     */
    fun <E : Throwable> error(exception: E?, message: (E?) -> Any? = { "" }) {
        if (exception == null) log.log(ERROR) { message(null) }
        else log.log(ERROR, exception, message)
    }

    /**
     * Set a logging level for this logger.
     *
     * @param level One of the logging levels identifiers, e.g., TRACE
     */
    fun setLoggerLevel(level: LoggingLevel) {
        LoggingManager.setLoggerLevel(name, level)
    }

    /**
     * Check if a logging level is enabled for this logger.
     *
     * @param level One of the logging levels identifiers, e.g., TRACE
     * @return True if the supplied level is enabled for this logger.
     */
    fun isLoggerLevelEnabled(level: LoggingLevel): Boolean =
        LoggingManager.isLoggerLevelEnabled(name, level)

    /**
     * Check if the [TRACE] logging level is enabled for this logger.
     *
     * @return True if the [TRACE] level is enabled for this logger.
     */
    fun isTraceEnabled(): Boolean =
        isLoggerLevelEnabled(TRACE)

    /**
     * Check if the [DEBUG] logging level is enabled for this logger.
     *
     * @return True if the [DEBUG] level is enabled for this logger.
     */
    fun isDebugEnabled(): Boolean =
        isLoggerLevelEnabled(DEBUG)

    /**
     * Check if the [INFO] logging level is enabled for this logger.
     *
     * @return True if the [INFO] level is enabled for this logger.
     */
    fun isInfoEnabled(): Boolean =
        isLoggerLevelEnabled(INFO)

    /**
     * Check if the [WARN] logging level is enabled for this logger.
     *
     * @return True if the [WARN] level is enabled for this logger.
     */
    fun isWarnEnabled(): Boolean =
        isLoggerLevelEnabled(WARN)

    /**
     * Check if the [ERROR] logging level is enabled for this logger.
     *
     * @return True if the [ERROR] level is enabled for this logger.
     */
    fun isErrorEnabled(): Boolean =
        isLoggerLevelEnabled(ERROR)
}
