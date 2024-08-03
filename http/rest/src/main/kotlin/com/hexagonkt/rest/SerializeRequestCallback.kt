package com.hexagonkt.rest

import com.hexagonkt.http.handlers.HttpCallback
import com.hexagonkt.http.handlers.HttpContext
import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.serialization.serialize

class SerializeRequestCallback : HttpCallback {

    init {
        check(SerializationManager.formats.isNotEmpty()) {
            "Serialization callbacks require at least one registered format"
        }
    }

    override fun invoke(context: HttpContext): HttpContext {
        val requestBody = context.request.body

        if (requestBody in emptyBodies)
            return context

        return context.request.contentType?.mediaType
            ?.let(SerializationManager::formatOfOrNull)
            ?.let { context.receive(body = requestBody.serialize(it)) }
            ?: context
    }
}
