package com.hexagonkt.rest

import com.hexagonkt.http.handlers.HttpContext
import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.serialization.serialize

class SerializeRequestCallback: (HttpContext) -> HttpContext {

    // TODO Short circuit if body is empty
    override fun invoke(context: HttpContext): HttpContext =
        context.request.contentType?.mediaType
            ?.let(SerializationManager::formatOfOrNull)
            ?.let { context.receive(body = context.request.body.serialize(it)) }
            ?: context
}
