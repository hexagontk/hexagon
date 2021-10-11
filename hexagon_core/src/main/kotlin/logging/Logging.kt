package com.hexagonkt.logging

/** Default logger for when you feel too lazy to declare one. */
val logger: Logger by lazy { Logger(Logger::class) }

/**
 * Uses this [T] to log a message with a prefix using [TRACE][LoggingLevel.TRACE] level.
 *
 * com.hexagonkt.logging.Logger must have TRACE level
 *
 * TODO Add use case and example in documentation.
 *
 * @receiver .
 * @param prefix .
 * @return .
 */
fun <T> T.trace(prefix: String = ""): T =
    apply { logger.trace { "$prefix$this" } }

/**
 * Uses this [T] to log a message with a prefix using [DEBUG][LoggingLevel.DEBUG] level.
 *
 * com.hexagonkt.logging.Logger must have DEBUG level
 *
 * TODO Add use case and example in documentation.
 *
 * @receiver .
 * @param prefix .
 * @return .
 */
fun <T> T.debug(prefix: String = ""): T =
    apply { logger.debug { "$prefix$this" } }

/**
 * Uses this [T] to log a message with a prefix using [INFO][LoggingLevel.INFO] level.
 *
 * com.hexagonkt.logging.Logger must have INFO level
 *
 * TODO Add use case and example in documentation.
 *
 * @receiver .
 * @param prefix .
 * @return .
 */
fun <T> T.info(prefix: String = ""): T =
    apply { logger.info { "$prefix$this" } }
