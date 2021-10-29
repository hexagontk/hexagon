package com.hexagonkt.core.serialization

class ParseException(val field: String, cause: Throwable? = null) : RuntimeException(cause)
