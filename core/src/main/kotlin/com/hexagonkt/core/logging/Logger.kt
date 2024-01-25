package com.hexagonkt.core.logging

import com.hexagonkt.core.text.stripAnsi
import java.lang.System.Logger.Level.*
import kotlin.reflect.KClass

/**
 * Logger class with Kotlin improvements like lazy evaluation. It is backed by a logging port.
 *
 * @param name Logger name. It is shown in the logs messages and used for log filtering.
 * @sample com.hexagonkt.core.logging.LoggerTest.loggerUsage
 */
class Logger(val name: String) {

    internal val log: System.Logger = System.getLogger(name)

    /**
     * Log a message, with associated exception information.
     */
    fun <E : Throwable> log(level: System.Logger.Level, exception: E, message: (E) -> Any?) {
        if (LoggingManager.useColor) message(exception)
        else message(exception)?.toString()?.stripAnsi()
        log.log(level, { message(exception)?.toString() }, exception)
    }

    /**
     * Log a message.
     */
    fun log(level: System.Logger.Level, message: () -> Any?) {
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
     * Log a message using [WARNING] level.
     *
     * @param message The required message to log.
     */
    fun warn(message: () -> Any?) {
        log.log(WARNING, message)
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
     * @param message The message to log (optional). If not supplied it will be empty.
     */
    fun <E : Throwable> error(exception: E?, message: (E?) -> Any? = { "" }) {
        if (exception == null) log(ERROR) { message(null) }
        else log(ERROR, exception, message)
    }
}
