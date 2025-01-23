package com.hexagontk.http.server.callbacks

import com.hexagontk.core.ResourceNotFoundException
import com.hexagontk.core.debug
import com.hexagontk.core.loggerOf
import com.hexagontk.core.media.mediaTypeOfOrNull
import com.hexagontk.core.require
import com.hexagontk.core.urlOf
import com.hexagontk.http.model.ContentType
import com.hexagontk.http.handlers.HttpContext
import java.lang.System.Logger
import java.net.URL

class UrlCallback(private val url: URL) : (HttpContext) -> HttpContext {

    constructor(url: String) : this(urlOf(url))

    private companion object {
        val logger: Logger = loggerOf(UrlCallback::class)
    }

    override fun invoke(context: HttpContext): HttpContext {
        val requestPath = when (context.pathParameters.size) {
            0 -> ""
            1 -> context.pathParameters.require("0")
            else -> error("URL loading require a single path parameter or none")
        }

        check(!requestPath.contains("..")) { "Requested path cannot contain '..': $requestPath" }
        logger.debug { "Resolving resource: $requestPath" }

        return try {
            if (requestPath.endsWith("/"))
                throw ResourceNotFoundException("$requestPath not found (folder)")

            val url = when {
                requestPath.isEmpty() -> url.toString()
                url.toString() == "classpath:/" -> "classpath:$requestPath"
                else -> "$url/$requestPath"
            }

            val resource = urlOf(url)
            val bytes = resource.readBytes()
            val mediaType = mediaTypeOfOrNull(resource)
            context.ok(bytes, contentType = mediaType?.let { ContentType(it) })
        }
        catch (e: ResourceNotFoundException) {
            context.notFound(e.message ?: "")
        }
    }
}
