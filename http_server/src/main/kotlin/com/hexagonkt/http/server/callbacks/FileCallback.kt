package com.hexagonkt.http.server.callbacks

import com.hexagonkt.core.media.mediaTypeOfOrNull
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.http.server.handlers.HttpServerContext
import java.io.File

class FileCallback(private val file: File) : (HttpServerContext) -> HttpServerContext {

    override fun invoke(context: HttpServerContext): HttpServerContext {
        val requestPath = context.pathParameters["0"]
            ?: error("File loading require a single path parameter")

        val file = file.resolve(requestPath).absoluteFile
        return if (file.exists()) {
            val bytes = file.readBytes()
            val mediaType = mediaTypeOfOrNull(file)
            context.ok(bytes, contentType = mediaType?.let { ContentType(it) })
        }
        else {
            context.notFound("File not found")
        }
    }
}
