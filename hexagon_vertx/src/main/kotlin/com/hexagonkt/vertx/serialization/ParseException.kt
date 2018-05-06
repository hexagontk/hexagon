package com.hexagonkt.vertx.serialization

import com.fasterxml.jackson.databind.JsonMappingException

class ParseException(cause: Throwable? = null) : RuntimeException(cause) {
    val field: String = (cause as? JsonMappingException)?.pathReference ?: ""
}
