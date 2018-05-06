package com.hexagonkt.vertx.config

import io.vertx.config.spi.ConfigStore
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer

class OptionalFileStore(
    private val vertx: Vertx,
    private val path: String,
    private val allowNotFound: Boolean
) : ConfigStore {

    override fun get(completionHandler: Handler<AsyncResult<Buffer>>) {
        val pathExists = vertx.fileSystem().existsBlocking(path)
        when {
            pathExists -> vertx.fileSystem().readFile(path, completionHandler)
            allowNotFound -> completionHandler.handle(Future.succeededFuture(Buffer.buffer()))
            else -> completionHandler.handle(Future.failedFuture("File '$path' not found"))
        }
    }
}
