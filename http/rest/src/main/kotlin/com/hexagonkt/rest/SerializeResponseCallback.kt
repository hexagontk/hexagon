package com.hexagonkt.rest

import com.hexagonkt.core.media.ANY_MEDIA
import com.hexagonkt.http.handlers.HttpCallback
import com.hexagonkt.http.handlers.HttpContext
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.serialization.serialize

class SerializeResponseCallback: HttpCallback {

    private companion object {
        val anyContentType = ContentType(ANY_MEDIA)
        val emptyBodies = setOf("", ByteArray(0))
    }

    override fun invoke(context: HttpContext): HttpContext {
        val responseBody = context.response.body

        if (responseBody in emptyBodies)
            return context

        return (context.request.accept - anyContentType)
            .ifEmpty { context.response.contentType?.let(::listOf) ?: emptyList() }
            .associateWith { SerializationManager.formatOfOrNull(it.mediaType) }
            .mapNotNull { (k, v) -> v?.let { k to it } }
            .firstOrNull()
            ?.let { (ct, sf) -> ct to responseBody.serialize(sf) }
            ?.let { (ct, c) -> context.send(body = c, contentType = ct) }
            ?: context
    }
}
