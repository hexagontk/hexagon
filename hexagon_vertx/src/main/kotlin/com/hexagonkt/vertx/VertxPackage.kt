package com.hexagonkt.vertx

import io.vertx.core.Future
import io.vertx.core.Verticle
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME
import io.vertx.core.logging.SLF4JLogDelegateFactory
import io.vertx.kotlin.core.DeploymentOptions
import java.util.function.Supplier

import com.fasterxml.jackson.core.JsonParser.Feature.*
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES
import com.fasterxml.jackson.databind.SerializationFeature.*

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.MapperFeature.SORT_PROPERTIES_ALPHABETICALLY
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.text.SimpleDateFormat

fun setupObjectMapper(objectMapper: ObjectMapper): ObjectMapper = objectMapper
    .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(FAIL_ON_EMPTY_BEANS, false)
    .configure(ALLOW_UNQUOTED_FIELD_NAMES, true)
    .configure(ALLOW_COMMENTS, true)
    .configure(ALLOW_SINGLE_QUOTES, true)
    .configure(FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
    .configure(ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
    .configure(SORT_PROPERTIES_ALPHABETICALLY, false)
    .setSerializationInclusion(NON_EMPTY)
    .setDateFormat(SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"))
    .registerModule(KotlinModule ())
    .registerModule(JavaTimeModule())
    .registerModule(Jdk8Module())

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
