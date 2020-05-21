package com.hexagonkt.helpers

/**
 * Exception with a list of causes. Cause is `null` as it can't be tell which one of the list is the
 * cause.
 *
 * A coded multiple exception should be created this way:
 * CodedException(400, "Many errors", MultipleException())
 *
 * To pass a list of causes
 * CodedException (500, "Error", *list)
 */
class MultipleException (val causes: List<Throwable>, message: String = "") :
    RuntimeException (message, null) {

    constructor(vararg causes: Throwable) : this(causes.toList())
    constructor(message: String, causes: List<Throwable>) : this(causes, message)
    constructor(message: String, vararg causes: Throwable) : this(causes.toList(), message)
}
