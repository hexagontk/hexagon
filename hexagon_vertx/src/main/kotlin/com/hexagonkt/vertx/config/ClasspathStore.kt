package com.hexagonkt.vertx.config

import io.vertx.config.spi.ConfigStore
import io.vertx.core.AsyncResult
import io.vertx.core.Future.*
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer

class ClasspathStore(private val path: String, private val allowNotFound: Boolean) : ConfigStore {
    init {
        require(!path.startsWith("/")) { "Classpath path cannot start with '/': $path" }
    }

    override fun get(completionHandler: Handler<AsyncResult<Buffer>>) {
        val urls = ClassLoader.getSystemClassLoader().getResources(path).toList()
        completionHandler.handle(
            when (urls.size) {
                1 ->
                    succeededFuture(Buffer.buffer(urls.first().readBytes()))
                0 ->
                    if (allowNotFound) succeededFuture(Buffer.buffer())
                    else failedFuture(IllegalStateException("No resources named '$path' found"))
                else ->
                    failedFuture(IllegalStateException("${urls.size} resources named '$path'"))
            }
        )
    }
}
