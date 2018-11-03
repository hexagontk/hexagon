package com.hexagonkt.helpers

/**
 * Exception with a numeric code.
 */
class CodedException (val code: Int, message: String = "", cause: Throwable? = null) :
    RuntimeException (message, cause)
