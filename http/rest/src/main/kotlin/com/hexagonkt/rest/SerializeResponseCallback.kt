package com.hexagontk.rest

import com.hexagontk.http.handlers.HttpCallback
import com.hexagontk.http.handlers.HttpContext
import com.hexagontk.serialization.SerializationManager
import com.hexagontk.serialization.serialize

// TODO Create SerializeResponseHandler like CorsHandler
class SerializeResponseCallback: HttpCallback {

    init {
        check(SerializationManager.formats.isNotEmpty()) {
            "Serialization callbacks require at least one registered format"
        }
    }

    override fun invoke(context: HttpContext): HttpContext {
        val responseBody = context.response.body

        if (responseBody is String || responseBody is ByteArray)
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
