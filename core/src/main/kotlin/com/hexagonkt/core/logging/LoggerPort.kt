package com.hexagonkt.core.logging

/**
 * A Logger is used to log messages for a specific system or application component.
 */
interface LoggerPort {

    /**
     * Log a message, with associated exception information.
     *
     * @param level One of the message level identifiers, e.g., TRACE.
     * @param exception The exception associated with log message.
     * @param message The required message to log.
     */
    fun <E : Throwable> log(level: LoggingLevel, exception: E, message: (E) -> Any?)

    /**
     * Log a message.
     *
     * @param level One of the message level identifiers, e.g., TRACE.
     * @param message The required message to log.
     */
    fun log(level: LoggingLevel, message: () -> Any?)
}
