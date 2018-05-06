package com.hexagonkt.vertx.http.client

import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.client.HttpRequest
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.ext.web.client.WebClientOptions

fun Vertx.createWebClient(
    port: Int = 80,
    host: String = "localhost",
    ssl: Boolean = false): WebClient =
        WebClient.create(this, WebClientOptions(defaultHost = host, defaultPort = port, ssl = ssl))

fun <T> HttpRequest<T>.send(): Future<HttpResponse<T>> =
    Future.future<HttpResponse<T>>().also { this.send(it) }

fun <T> HttpRequest<T>.sendBuffer(body: Buffer): Future<HttpResponse<T>> =
    Future.future<HttpResponse<T>>().also { this.sendBuffer(body, it) }
