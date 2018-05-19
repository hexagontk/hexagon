package com.hexagonkt.vertx.config

import io.vertx.config.spi.ConfigStore
import io.vertx.config.spi.ConfigStoreFactory
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject

class OptionalFileStoreFactory : ConfigStoreFactory {
    override fun create(vertx: Vertx, configuration: JsonObject): ConfigStore =
        OptionalFileStore(
            vertx,
            configuration.getString("path") ?: error("Classpath store needs 'path'"),
            configuration.getBoolean("allowNotFound") ?: false
        )

    override fun name(): String = "optionalFile"
}
