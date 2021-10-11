package com.hexagonkt.logging

/**
 * Logging Contract for integrating different logging libraries.
 */
interface LoggingPort {

    /**
     * Create [Logger][LoggerPort] with name.
     *
     * @param name Logger name.
     */
    fun createLogger(name: String): LoggerPort

    /**
     * Set logging level for a logger.
     *
     * @param name Logger name.
     * @param level One of the logging levels identifiers, e.g., TRACE
     */
    fun setLoggerLevel(name: String, level: LoggingLevel)
}
