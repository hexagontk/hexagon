package com.hexagonkt.http.server.nima

import com.hexagonkt.http.client.jetty.JettyClientAdapter
import com.hexagonkt.http.test.examples.*
import com.hexagonkt.serialization.jackson.JacksonTextFormat
import com.hexagonkt.serialization.jackson.json.Json
import com.hexagonkt.serialization.jackson.yaml.Yaml
import org.junit.jupiter.api.Disabled

val clientAdapter: () -> JettyClientAdapter = ::JettyClientAdapter
val serverAdapter: () -> NimaServerAdapter = ::NimaServerAdapter
val formats: List<JacksonTextFormat> = listOf(Json, Yaml)

internal class AdapterBooksTest : BooksTest(clientAdapter, serverAdapter)
internal class AdapterErrorsTest : ErrorsTest(clientAdapter, serverAdapter)
internal class AdapterFiltersTest : FiltersTest(clientAdapter, serverAdapter)
@Disabled
internal class AdapterClientTest : ClientTest(clientAdapter, serverAdapter, formats)
internal class AdapterHttpsTest : HttpsTest(clientAdapter, serverAdapter)
@Disabled
internal class AdapterZipTest : ZipTest(clientAdapter, serverAdapter)
@Disabled
internal class AdapterCookiesTest : CookiesTest(clientAdapter, serverAdapter)
@Disabled
internal class AdapterFilesTest : FilesTest(clientAdapter, serverAdapter)
internal class AdapterCorsTest : CorsTest(clientAdapter, serverAdapter)
@Disabled
internal class AdapterSamplesTest : SamplesTest(clientAdapter, serverAdapter)
internal class AdapterBenchmarkIT : BenchmarkIT(clientAdapter, serverAdapter)
@Disabled
internal class AdapterSseTest : SseTest(clientAdapter, serverAdapter)
@Disabled
internal class AdapterWebSocketsTest : WebSocketsTest(clientAdapter, serverAdapter)
