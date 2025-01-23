package com.hexagontk.http.server.callbacks

import com.hexagontk.core.debug
import com.hexagontk.core.loggerOf
import com.hexagontk.core.media.mediaTypeOfOrNull
import com.hexagontk.core.require
import com.hexagontk.http.model.ContentType
import com.hexagontk.http.handlers.HttpContext
import java.io.File
import java.lang.System.Logger

/**
 * Callback that resolves requests' path parameters to files based on a base file. Requests path
 * parameters are not allowed to contain `..` (references to [file] parent directories are not
 * permitted).
 *
 * If request does not have path parameters [file] will be returned (or not found if [file] is a
 * directory).
 *
 * @param file Base file used to resolve paths passed on the request.
 */
class FileCallback(private val file: File) : (HttpContext) -> HttpContext {

    constructor(file: String) : this(File(file))

    private companion object {
        val logger: Logger = loggerOf(FileCallback::class)
    }

    override fun invoke(context: HttpContext): HttpContext {
        val file = when (context.pathParameters.size) {
            0 -> file.absoluteFile
            1 -> {
                val relativePath = context.pathParameters.require("0")
                check(!relativePath.contains("..")) {
                    "Requested path cannot contain '..': $relativePath"
                }
                file.resolve(relativePath).absoluteFile
            }
            else -> error("File loading require a single path parameter or none")
        }
        val fileName = file.name

        logger.debug { "Resolving file: $fileName" }

        return if (file.exists() && file.isFile) {
            val bytes = file.readBytes()
            val mediaType = mediaTypeOfOrNull(file)
            context.ok(bytes, contentType = mediaType?.let { ContentType(it) })
        }
        else {
            context.notFound("File '$fileName' not found")
        }
    }
}
