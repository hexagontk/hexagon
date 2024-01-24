package com.hexagonkt.core

/**
 * Exception with a numeric code.
 *
 * @property code Exception code.
 * @property message Error message.
 * @property cause Parent exception.
 */
class CodedException(val code: Int, message: String = "", cause: Throwable? = null) :
    RuntimeException(message, cause)
