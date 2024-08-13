package com.hexagonkt.http.test.examples

import com.hexagonkt.core.urlOf
import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.client.HttpClientSettings
import com.hexagonkt.http.model.HttpRequest
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.HttpMethod.*
import com.hexagonkt.http.server.HttpServer
import com.hexagonkt.http.server.HttpServerPort
import com.hexagonkt.http.server.HttpServerSettings
import org.junit.jupiter.api.Test

abstract class MultipartSamplesTest(
    val clientAdapter: () -> HttpClientPort,
    val serverAdapter: () -> HttpServerPort,
    val serverSettings: HttpServerSettings = HttpServerSettings(),
) {
    @Test open fun callbacks() {
        val server = HttpServer(serverAdapter()) {
            // callbackFile
            post("/file") {
                val filePart = request.partsMap()["file"] ?: error("File not available")
                ok(filePart.body)
            }
            // callbackFile
        }

        server.use { s ->
            s.start()
            HttpClient(clientAdapter(), HttpClientSettings(s.binding)).use {
                it.start()
                val stream = urlOf("classpath:assets/index.html").readBytes()
                val parts = listOf(HttpPart("file", stream, "index.html"))
                val response = it.send(HttpRequest(POST, path = "/file", parts = parts))
                assert(response.bodyString().contains("<title>Hexagon</title>"))
            }
        }
    }
}
