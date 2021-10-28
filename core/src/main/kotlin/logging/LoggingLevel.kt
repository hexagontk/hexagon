package com.hexagonkt.logging

/**
 * Logger logging level values.
 *
 * @property TRACE Used for low level details that are logged very often.
 * @property DEBUG Useful information to diagnose problems or failures.
 * @property INFO Only used for really useful information that is not written very often.
 * @property WARN To notify that something failed and was ignored, but it could be an issue later.
 * @property ERROR Error that stopped the correct processing of the process.
 * @property OFF Disable all logging levels.
 */
enum class LoggingLevel {
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
    OFF,
}
