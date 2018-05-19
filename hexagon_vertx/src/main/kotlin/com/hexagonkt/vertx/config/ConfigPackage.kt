package com.hexagonkt.vertx.config

import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigStoreOptions
import io.vertx.core.Future
import io.vertx.core.Future.future
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.config.ConfigRetrieverOptions
import io.vertx.kotlin.config.ConfigStoreOptions
import io.vertx.kotlin.core.json.JsonObject

fun retrieveConfig(configRetriever: ConfigRetriever): Future<JsonObject> {
    val f = future<JsonObject>()
    configRetriever.getConfig(f)
    return f
}

fun Vertx.retrieveConfig(vararg args: String): Future<JsonObject> =
    retrieveConfig(createConfigRetriever(*args))

fun Vertx.createConfigRetriever(vararg args: String): ConfigRetriever {
    val stores = ConfigRetrieverOptions(stores = defaultStores(*args))
    return ConfigRetriever.create(this, stores)
}

fun defaultStores(vararg args: String): List<ConfigStoreOptions> = listOf(
    classpathStoreOptions("service.yaml"),
    ConfigStoreOptions(type = "env"),
    ConfigStoreOptions(type = "sys"),
    optionalFileStoreOptions("service.yaml"),
    commandLineStoreOptions(*args),
    classpathStoreOptions("service_test.yaml")
)

fun commandLineStoreOptions(vararg args: String) = ConfigStoreOptions(
    type = "commandLine",
    config = JsonObject("args" to args)
)

fun classpathStoreOptions(path: String, allowNotFound: Boolean = true) =
    pathStoreOptions("classpath", path, allowNotFound)

fun optionalFileStoreOptions(path: String, allowNotFound: Boolean = true) =
    pathStoreOptions("optionalFile", path, allowNotFound)

private fun pathStoreOptions(type: String, path: String, allowNotFound: Boolean = true) =
    ConfigStoreOptions(
        type = type,
        format = if (path.endsWith("json")) "json" else "yaml",
        config = JsonObject("path" to path, "allowNotFound" to allowNotFound)
    )
