package com.hexagonkt.http.test

import com.hexagonkt.core.media.ApplicationMedia.JSON
import com.hexagonkt.core.media.TextMedia.PLAIN
import com.hexagonkt.core.require
import com.hexagonkt.http.model.ClientErrorStatus.NOT_FOUND
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.http.server.HttpServer
import com.hexagonkt.http.server.HttpServerPort
import com.hexagonkt.http.server.callbacks.FileCallback
import com.hexagonkt.http.server.handlers.PathHandler
import com.hexagonkt.http.server.serve
import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.serialization.parse
import com.hexagonkt.serialization.serialize
import java.io.File
import java.nio.file.Path

data class Server(
    val adapter: HttpServerPort,
    var path: PathHandler = PathHandler()
) {

    private val server: HttpServer by lazy {
        val servedPath: Path = File(System.getProperty("user.dir")).toPath()

        serve(adapter) {
            after(pattern = "*", status = NOT_FOUND) {
                // Dynamic resolution picking from path: PathHandler
                copy(
                    context = context.copy(
                        event = context.event.copy(
                            response = this@Server.path.process(request)
                        )
                    )
                )
            }
        }
    }
}
