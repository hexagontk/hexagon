package com.hexagonkt.vertx

import com.hexagonkt.vertx.serialization.JacksonHelper.setupObjectMapper
import io.vertx.core.Future
import io.vertx.core.Verticle
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME
import io.vertx.core.logging.SLF4JLogDelegateFactory
import io.vertx.kotlin.core.DeploymentOptions
import java.util.function.Supplier

fun createVertx(): Vertx {
    System.setProperty(LOGGER_DELEGATE_FACTORY_CLASS_NAME, SLF4JLogDelegateFactory::class.java.name)
    setupObjectMapper(Json.mapper)
    return Vertx.vertx()
}

fun Vertx.deployVerticle(supplier: () -> Verticle, config: JsonObject): Future<String> {
    val future = Future.future<String>()
    this.deployVerticle(Supplier { supplier() }, DeploymentOptions(config = config), future)
    return future
}

fun Map<String, *>.toJsonObject(): JsonObject = JsonObject(this)
