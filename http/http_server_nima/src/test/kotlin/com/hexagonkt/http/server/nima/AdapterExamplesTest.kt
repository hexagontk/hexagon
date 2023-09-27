package com.hexagonkt.http.server.nima

import com.hexagonkt.core.urlOf
import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientSettings
import com.hexagonkt.http.client.jetty.JettyClientAdapter
import com.hexagonkt.http.server.HttpServer
import com.hexagonkt.http.test.examples.*
import com.hexagonkt.serialization.jackson.JacksonTextFormat
import com.hexagonkt.serialization.jackson.json.Json
import com.hexagonkt.serialization.jackson.yaml.Yaml
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledOnOs
import org.junit.jupiter.api.condition.OS.WINDOWS
import kotlin.test.assertEquals
import kotlin.test.assertNull

val clientAdapter: () -> JettyClientAdapter = ::JettyClientAdapter
val serverAdapter: () -> NimaServerAdapter = ::NimaServerAdapter
val formats: List<JacksonTextFormat> = listOf(Json, Yaml)

internal class AdapterBooksTest : BooksTest(clientAdapter, serverAdapter)
internal class AdapterErrorsTest : ErrorsTest(clientAdapter, serverAdapter)
internal class AdapterFiltersTest : FiltersTest(clientAdapter, serverAdapter)
internal class AdapterClientTest : ClientTest(clientAdapter, serverAdapter, formats) {
    @Test @Disabled override fun `Form parameters are sent correctly`() {}
}
@DisabledOnOs(WINDOWS) // TODO Make this work on GitHub runners
internal class AdapterHttpsTest : HttpsTest(clientAdapter, serverAdapter)
internal class AdapterZipTest : ZipTest(clientAdapter, serverAdapter) {
    @Test override fun `Use ZIP encoding without enabling the feature example`() {

        val server = HttpServer(serverAdapter(), serverSettings.copy(bindPort = 0)) {
            get("/hello") {
                ok("Hello World!")
            }
        }
        server.start()

        val settings = HttpClientSettings(urlOf("http://localhost:${server.runtimePort}"))
        val client = HttpClient(clientAdapter(), settings)
        client.start()

        client.get("/hello").apply {
            assertEquals(body, "Hello World!")
            assertNull(headers["content-encoding"])
            assertNull(headers["Content-Encoding"])
        }

        client.stop()
        server.stop()
    }
}
internal class AdapterCookiesTest : CookiesTest(clientAdapter, serverAdapter)
internal class AdapterFilesTest : FilesTest(clientAdapter, serverAdapter)
internal class AdapterCorsTest : CorsTest(clientAdapter, serverAdapter)
internal class AdapterSamplesTest : SamplesTest(clientAdapter, serverAdapter)
internal class AdapterBenchmarkIT : BenchmarkIT(clientAdapter, serverAdapter)
@Disabled
internal class AdapterSseTest : SseTest(clientAdapter, serverAdapter)
@Disabled
internal class AdapterWebSocketsTest : WebSocketsTest(clientAdapter, serverAdapter)
