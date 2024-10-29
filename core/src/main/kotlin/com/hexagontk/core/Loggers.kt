package com.hexagontk.core

import com.hexagontk.core.text.stripAnsi
import java.lang.System.Logger
import java.lang.System.Logger.Level
import java.lang.System.Logger.Level.*
import kotlin.reflect.KClass

internal val useColor: Boolean by lazy { Platform.systemSetting("hexagontk_logging_color", true) }
internal val defaultLoggerName: String by lazy {
    Platform.systemSetting("hexagontk_logging_logger_name", "com.hexagontk.core.logging")
}

/** Default logger for when you feel too lazy to declare one. */
val logger: Logger by lazy { loggerOf(defaultLoggerName) }

/**
 * Use this [T] to log a message with a prefix using [TRACE] level.
 *
 * [com.hexagontk.core.logger] must have the [TRACE] level enabled.
 *
 * @receiver Object which string representation will be logged.
 * @param T Type of the logged object.
 * @param prefix Prefix for the logging message.
 * @return The receiver reference for chaining methods.
 */
fun <T> T.trace(prefix: String = ""): T =
    apply { logger.trace { "$prefix$this" } }

/**
 * Use this [T] to log a message with a prefix using [DEBUG] level.
 *
 * [com.hexagontk.core.logger] must have the [DEBUG] level enabled.
 *
 * @receiver Object which string representation will be logged.
 * @param T Type of the logged object.
 * @param prefix Prefix for the logging message.
 * @return The receiver reference for chaining methods.
 */
fun <T> T.debug(prefix: String = ""): T =
    apply { logger.debug { "$prefix$this" } }

/**
 * Use this [T] to log a message with a prefix using [INFO] level.
 *
 * [com.hexagontk.core.logger] must have the [INFO] level enabled.
 *
 * @receiver Object which string representation will be logged.
 * @param T Type of the logged object.
 * @param prefix Prefix for the logging message.
 * @return The receiver reference for chaining methods.
 */
fun <T> T.info(prefix: String = ""): T =
    apply { logger.info { "$prefix$this" } }

/**
 * Logger constructor function.
 *
 * @param type Logger type. It is shown in the logs messages and used for log filtering.
 */
fun loggerOf(type: KClass<*>): Logger =
    loggerOf(type.qualifiedName ?: error("Cannot get qualified name of type"))

/**
 * Logger constructor function.
 *
 * @param name Logger name. It is shown in the logs messages and used for log filtering.
 */
fun loggerOf(name: String): Logger =
    System.getLogger(name)

/**
 * Log a message, with associated exception information.
 *
 * @param level Level used in the log statement.
 * @param exception The exception associated with log message.
 * @param message The message supplier to use in the log statement.
 */
fun <E : Throwable> Logger.log(level: Level, exception: E, message: (E) -> Any?) {
    val messageSupplier = { stripAnsi(message(exception), useColor) }
    log(level, messageSupplier, exception)
}

/**
 * Log a message using [TRACE] level.
 *
 * @param message The required message to log.
 */
fun Logger.trace(message: () -> Any?) {
    logMessage(TRACE, message)
}

/**
 * Log a message using [DEBUG] level.
 *
 * @param message The required message to log.
 */
fun Logger.debug(message: () -> Any?) {
    logMessage(DEBUG, message)
}

/**
 * Log a message using [INFO] level.
 *
 * @param message The required message to log.
 */
fun Logger.info(message: () -> Any?) {
    logMessage(INFO, message)
}

/**
 * Log a message using [WARNING] level.
 *
 * @param message The required message to log.
 */
fun Logger.warn(message: () -> Any?) {
    logMessage(WARNING, message)
}

/**
 * Log a message using [ERROR] level.
 *
 * @param message The required message to log.
 */
fun Logger.error(message: () -> Any?) {
    logMessage(ERROR, message)
}

/**
 * Log a message using [WARNING] level with associated exception information.
 *
 * @param exception The exception associated with log message.
 * @param message The message to log (optional). If not supplied it will be empty.
 */
fun <E : Throwable> Logger.warn(exception: E?, message: (E?) -> Any? = { "" }) {
    if (exception == null) log(WARNING) { message(null)?.toString() }
    else log(WARNING, exception, message)
}

/**
 * Log a message using [ERROR] level with associated exception information.
 *
 * @param exception The exception associated with log message.
 * @param message The message to log (function to optional). If not supplied it will be empty.
 */
fun <E : Throwable> Logger.error(exception: E?, message: (E?) -> Any? = { "" }) {
    if (exception == null) log(ERROR) { message(null)?.toString() }
    else log(ERROR, exception, message)
}

internal fun <T> stripAnsi(receiver: T?, apply: Boolean): String? =
    receiver?.toString()?.let { if (apply) it.stripAnsi() else it }

private fun Logger.logMessage(level: Level, message: () -> Any?) {
    val messageSupplier = { stripAnsi(message(), useColor) }
    log(level, messageSupplier)
}
