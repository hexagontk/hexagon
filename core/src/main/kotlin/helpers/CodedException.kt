package com.hexagonkt.core.helpers

/**
 * Exception with a numeric code.
 *
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @property code .
 * @property message .
 * @property cause .
 */
class CodedException (val code: Int, message: String = "", cause: Throwable? = null) :
    RuntimeException (message, cause)
