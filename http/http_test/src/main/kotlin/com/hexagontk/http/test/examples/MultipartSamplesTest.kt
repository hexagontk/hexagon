package com.hexagontk.http.test.examples

import com.hexagontk.core.urlOf
import com.hexagontk.http.client.HttpClient
import com.hexagontk.http.client.HttpClientPort
import com.hexagontk.http.client.HttpClientSettings
import com.hexagontk.http.model.HttpRequest
import com.hexagontk.http.model.*
import com.hexagontk.http.model.HttpMethod.*
import com.hexagontk.http.server.HttpServer
import com.hexagontk.http.server.HttpServerPort
import com.hexagontk.http.server.HttpServerSettings
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
