package com.hexagonkt.rest

import com.hexagonkt.http.handlers.HttpCallback
import com.hexagonkt.http.handlers.HttpContext
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.serialization.serialize

class SerializeResponseCallback: HttpCallback {

    fun HttpContext.accept(): List<ContentType> =
        request.accept.ifEmpty { response.contentType?.let(::listOf) ?: emptyList() }

    // TODO Short circuit if body is empty
    override fun invoke(context: HttpContext): HttpContext =
        context.accept()
            .associateWith { SerializationManager.formatOfOrNull(it.mediaType) }
            .mapNotNull { (k, v) -> v?.let { k to it } }
            .firstOrNull()
            ?.let { (ct, sf) -> ct to context.response.body.serialize(sf) }
            ?.let { (ct, c) -> context.send(body = c, contentType = ct) }
            ?: context
}
