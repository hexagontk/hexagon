package com.hexagonkt.serialization

class ParseException(val field: String, cause: Throwable? = null) : RuntimeException(cause)
