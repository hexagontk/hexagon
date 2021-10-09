package com.hexagonkt.logging

/**
 * Logging Contract for:
 * - Creating logger [createLogger].
 * - Setting the logging level [setLoggerLevel].
 */
interface LoggingPort {

    /**
     * Creates [LoggerPort] by name.
     *
     * @param [name] Logger name.
     */
    fun createLogger(name: String): LoggerPort

    /**
     * Indicates logging level of a logger.
     *
     * @param [name] Logger name.
     * @param [level] One of the logging levels identifiers, e.g., TRACE
     *
     * @see [LoggingLevel]
     */
    fun setLoggerLevel(name: String, level: LoggingLevel)
}
