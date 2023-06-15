package com.hexagonkt.rest

import com.hexagonkt.http.model.HttpBase
import com.hexagonkt.http.handlers.HttpContext
import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.serialization.parseList
import com.hexagonkt.serialization.parseMap
import com.hexagonkt.serialization.serialize

fun HttpBase.bodyList(): List<*> =
    bodyString().parseList(mediaType())

fun HttpBase.bodyMap(): Map<*, *> =
    bodyString().parseMap(mediaType())

fun HttpBase.mediaType() =
    contentType?.mediaType ?: SerializationManager.requireDefaultFormat().mediaType

val serializeCallback: (HttpContext) -> HttpContext = { context ->
    context.request.contentType?.mediaType
        ?.let(SerializationManager::formatOfOrNull)
        ?.let { context.request(body = context.request.body.serialize(it)) }
        ?: context
}
