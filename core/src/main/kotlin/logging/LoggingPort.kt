package com.hexagonkt.core.logging

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

    /**
     * Check if a logging level is enabled for a logger.
     *
     * @param name Logger name.
     * @param level One of the logging levels identifiers, e.g., TRACE
     * @return True if the supplied level is enabled for the passed logger name.
     */
    fun isLoggerLevelEnabled(name: String, level: LoggingLevel): Boolean
}
