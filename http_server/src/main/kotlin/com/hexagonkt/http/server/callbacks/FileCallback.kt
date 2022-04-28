package com.hexagonkt.http.server.callbacks

import com.hexagonkt.core.media.mediaTypeOfOrNull
import com.hexagonkt.core.require
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.http.server.handlers.HttpServerContext
import java.io.File

class FileCallback(private val file: File) : (HttpServerContext) -> HttpServerContext {

    override fun invoke(context: HttpServerContext): HttpServerContext {
        val file = when (context.pathParameters.size) {
            0 -> file.absoluteFile
            1 -> file.resolve(context.pathParameters.require("0")).absoluteFile
            else -> error("File loading require a single path parameter or none")
        }
        return if (file.exists() && file.isFile) {
            val bytes = file.readBytes()
            val mediaType = mediaTypeOfOrNull(file)
            context.ok(bytes, contentType = mediaType?.let { ContentType(it) })
        }
        else {
            context.notFound("File '${file.name}' not found")
        }
    }
}
