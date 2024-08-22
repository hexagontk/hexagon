package com.hexagontk.rest

import com.hexagontk.http.handlers.HttpCallback
import com.hexagontk.http.handlers.HttpContext
import com.hexagontk.serialization.SerializationManager
import com.hexagontk.serialization.serialize

// TODO Create SerializeRequestHandler like CorsHandler
class SerializeRequestCallback : HttpCallback {

    init {
        check(SerializationManager.formats.isNotEmpty()) {
            "Serialization callbacks require at least one registered format"
        }
    }

    override fun invoke(context: HttpContext): HttpContext {
        val requestBody = context.request.body

        if (requestBody is String || requestBody is ByteArray)
            return context

        return context.request.contentType?.mediaType
            ?.let(SerializationManager::formatOfOrNull)
            ?.let { context.receive(body = requestBody.serialize(it)) }
            ?: context
    }
}
