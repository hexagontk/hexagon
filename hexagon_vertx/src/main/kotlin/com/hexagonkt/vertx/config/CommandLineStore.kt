package com.hexagonkt.vertx.config

import io.vertx.config.spi.ConfigStore
import io.vertx.config.spi.ConfigStoreFactory
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject

class CommandLineStore(private val args: List<String> = emptyList()) :
    ConfigStoreFactory, ConfigStore {

    override fun get(completionHandler: Handler<AsyncResult<Buffer>>) {
        completionHandler.handle(
            Future.succeededFuture(
                Buffer.buffer(loadCommandLine(*args.toTypedArray()).encode())
            )
        )
    }

    override fun create(vertx: Vertx, configuration: JsonObject): ConfigStore =
        configuration.getValue("args").let {
            CommandLineStore(
                when (it) {
                    is Array<*> -> it.map { it.toString() }.toList()
                    is String -> listOf(it)
                    else -> emptyList()
                }
            )
        }

    override fun name(): String = "commandLine"

    private fun loadCommandLine (vararg args: String): JsonObject =
        JsonObject(
            args
                .map { it.removePrefix("--") }
                .filter { !it.startsWith("=") }
                .map { it.split("=") }
                .filter { it.size <= 2 }
                .map { if (it.size == 1) it[0] to true else it[0] to it[1] }
                .toMap()
        )
}
