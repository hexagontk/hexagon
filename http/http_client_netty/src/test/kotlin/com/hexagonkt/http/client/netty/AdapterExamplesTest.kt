package com.hexagonkt.http.client.netty

import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.server.HttpServerPort
import com.hexagonkt.http.server.jetty.JettyServletAdapter
import com.hexagonkt.http.test.examples.*
import com.hexagonkt.serialization.jackson.JacksonTextFormat
import com.hexagonkt.serialization.jackson.json.Json
import com.hexagonkt.serialization.jackson.yaml.Yaml
import org.junit.jupiter.api.condition.DisabledInNativeImage
import org.junit.jupiter.api.condition.DisabledOnOs
import org.junit.jupiter.api.condition.OS.WINDOWS

// TODO Assert context methods (request.method, request.protocol...)
// TODO Check response headers don't contain invalid chars (\n, \t...)

val clientAdapter: () -> HttpClientPort = ::NettyClientAdapter
val serverAdapter: () -> HttpServerPort = ::JettyServletAdapter
val formats: List<JacksonTextFormat> = listOf(Json, Yaml)

//internal class AdapterBooksTest : BooksTest(clientAdapter, serverAdapter)
//internal class AdapterErrorsTest : ErrorsTest(clientAdapter, serverAdapter)
//internal class AdapterFiltersTest : FiltersTest(clientAdapter, serverAdapter)
//internal class AdapterClientTest : ClientTest(clientAdapter, serverAdapter, formats)
//@DisabledOnOs(WINDOWS) // TODO Make this work on GitHub runners
//internal class AdapterHttpsTest : HttpsTest(clientAdapter, serverAdapter)
//internal class AdapterZipTest : ZipTest(clientAdapter, serverAdapter)
//internal class AdapterCookiesTest : CookiesTest(clientAdapter, serverAdapter)
//internal class AdapterFilesTest : FilesTest(clientAdapter, serverAdapter)
//internal class AdapterCorsTest : CorsTest(clientAdapter, serverAdapter)
//internal class AdapterSamplesTest : SamplesTest(clientAdapter, serverAdapter)
//internal class AdapterBenchmarkIT : BenchmarkIT(clientAdapter, serverAdapter)
//internal class AdapterSseTest : SseTest(clientAdapter, serverAdapter)
//@DisabledInNativeImage // TODO Fix this
//internal class AdapterWebSocketsTest : WebSocketsTest(clientAdapter, serverAdapter)
