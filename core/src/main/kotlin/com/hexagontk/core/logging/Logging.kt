package com.hexagontk.core.logging

import com.hexagontk.core.Jvm

internal val useColor: Boolean by lazy { Jvm.systemSetting("hexagontk_logging_color", true) }
internal val defaultLoggerName: String by lazy {
    Jvm.systemSetting("hexagontk_logging_logger_name", "com.hexagontk.core.logging")
}

/** Default logger for when you feel too lazy to declare one. */
val logger: Logger by lazy { Logger(defaultLoggerName) }

/**
 * Use this [T] to log a message with a prefix using [TRACE] level.
 *
 * [com.hexagontk.core.logging.logger] must have the [TRACE] level enabled.
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
 * [com.hexagontk.core.logging.logger] must have the [DEBUG] level enabled.
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
 * [com.hexagontk.core.logging.logger] must have the [INFO] level enabled.
 *
 * @receiver Object which string representation will be logged.
 * @param T Type of the logged object.
 * @param prefix Prefix for the logging message.
 * @return The receiver reference for chaining methods.
 */
fun <T> T.info(prefix: String = ""): T =
    apply { logger.info { "$prefix$this" } }
