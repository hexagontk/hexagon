package com.hexagonkt.vertx.http

import com.hexagonkt.helpers.CodedException
import com.hexagonkt.serialization.SerializationFormat
import com.hexagonkt.serialization.SerializationManager.defaultFormat
import com.hexagonkt.serialization.SerializationManager.formatOf
import com.hexagonkt.serialization.parse
import com.hexagonkt.serialization.parseList
import com.hexagonkt.serialization.serialize
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import kotlin.reflect.KClass

typealias Callback = RoutingContext.() -> Unit

typealias TypedCallback<T> = RoutingContext.(T?) -> Any?

val HttpServerRequest.contentType: String?
    get() = getHeader("Content-Type")

var HttpServerResponse.contentType: String?
    get() =
        headers()["Content-Type"]
    set(value) {
        headers()["Content-Type"] = value
    }

fun RoutingContext.end(status: Int, message: String) {
    response.end(status, message)
}

val RoutingContext.request: HttpServerRequest get() = this.request()

val RoutingContext.response: HttpServerResponse get() = this.response()

fun HttpServerResponse.end(status: Int, message: String) {
    statusCode = status
    end(message)
}

fun Router.get(callback: Callback): Route = this.get().handler(callback)

fun Router.get(path: String, callback: Callback): Route = this.get(path).handler(callback)

fun Router.put(callback: Callback): Route = this.put().handler(callback)

fun Router.put(path: String, callback: Callback): Route = this.put(path).handler(callback)

fun Router.patch(callback: Callback): Route = this.patch().handler(callback)

fun Router.patch(path: String, callback: Callback): Route = this.patch(path).handler(callback)

fun Router.delete(callback: Callback): Route = this.delete().handler(callback)

fun Router.delete(path: String, callback: Callback): Route = this.delete(path).handler(callback)

fun Router.post(callback: Callback): Route = this.post().handler(callback)

fun Router.post(path: String, callback: Callback): Route = this.post(path).handler(callback)

fun Router.put(callback: Handler<RoutingContext>): Route =
    this.put().handler(callback)

fun Router.post(callback: Handler<RoutingContext>): Route =
    this.post().handler(callback)

fun <T : Any> Router.post(type: KClass<T>, block: TypedCallback<T>): Route =
    post { handle(type, block) }

fun <T : Any> Router.post(type: KClass<T>, path: String, block: TypedCallback<T>): Route =
    post(path) { handle(type, block) }

fun <T : Any> RoutingContext.handle(type: KClass<T>, block: RoutingContext.(T?) -> Any?) {
    val requestContentFormat = formatOf(this.request().contentType, defaultFormat)
    val entity = bodyAsString.parse(type, requestContentFormat)
    endWith(block(entity))
}

fun <T : Any> RoutingContext.handleList(type: KClass<T>, block: RoutingContext.(List<T>) -> Any?) {
    val requestContentFormat = formatOf(this.request().contentType, defaultFormat)
    val entities =
        if (bodyAsString.isNullOrBlank()) emptyList()
        else bodyAsString.parseList(type, requestContentFormat)
    endWith(block(entities))
}

fun RoutingContext.handle(block: RoutingContext.() -> Any?) {
    endWith(block())
}

fun RoutingContext.acceptFormat(): SerializationFormat {
    val s = response.contentType ?: request().getHeader("Accept") ?: request.contentType
    return formatOf(s, defaultFormat)
}

fun RoutingContext.endWith(r: Any?) {
    // TODO Take care of different result types (Exception -throw it- or Pair Code -> Msg)
    val result = r ?: throw CodedException(404, "${request().path()} not found")
    val contentFormat = acceptFormat()

    when (result) {
        is Future<*> -> result.setHandler {
            val result1 = it.result()
            when (result1) {
                is String, Long, Int, Float, Double -> end(200, result1.toString())
                is Boolean -> end(200, result1.toString())
                else -> end(200, result1.serialize(contentFormat))
            }

        }
        is String, Long, Int, Float, Double -> end(200, result.toString())
        is Boolean -> end(200, result.toString())
        else -> end(200, result.serialize(contentFormat))
    }
}

fun <T> RoutingContext.handler(
    block: RoutingContext.(T?) -> Unit): (AsyncResult<T>) -> Unit = {

    if (it.succeeded()) block(it.result())
    else end(500, "Internal Error: ${it.cause().message}")
}

fun notImplemented(context: RoutingContext) {
    context.response().end(501, "Not implemented")
}
