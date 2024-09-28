package com.hexagontk.http.client.java

import com.hexagontk.http.client.HttpClientPort
import com.hexagontk.http.server.HttpServerPort
import com.hexagontk.http.server.jetty.JettyServletHttpServer
import com.hexagontk.http.test.examples.*
import com.hexagontk.serialization.jackson.JacksonTextFormat
import com.hexagontk.serialization.jackson.json.Json
import com.hexagontk.serialization.jackson.yaml.Yaml

val clientAdapter: () -> HttpClientPort = ::JavaHttpClient
val serverAdapter: () -> HttpServerPort = ::JettyServletHttpServer
val formats: List<JacksonTextFormat> = listOf(Json, Yaml)

// TODO Add multipart and file upload tests
internal class AdapterBooksTest : BooksTest(clientAdapter, serverAdapter)
internal class AdapterErrorsTest : ErrorsTest(clientAdapter, serverAdapter)
internal class AdapterFiltersTest : FiltersTest(clientAdapter, serverAdapter)
internal class AdapterClientTest : ClientTest(clientAdapter, serverAdapter, formats)
internal class AdapterClientCookiesTest : ClientCookiesTest(clientAdapter, serverAdapter, formats)
internal class AdapterClientHttp2Test : ClientHttp2Test(clientAdapter, serverAdapter, formats)
internal class AdapterClientHttpsTest : ClientHttpsTest(clientAdapter, serverAdapter, formats)
internal class AdapterHttpsTest : HttpsTest(clientAdapter, serverAdapter)
internal class AdapterZipTest : ZipTest(clientAdapter, serverAdapter)
internal class AdapterCookiesTest : CookiesTest(clientAdapter, serverAdapter)
internal class AdapterFilesTest : FilesTest(clientAdapter, serverAdapter)
internal class AdapterCorsTest : CorsTest(clientAdapter, serverAdapter)
internal class AdapterSamplesTest : SamplesTest(clientAdapter, serverAdapter)
internal class AdapterBenchmarkIT : BenchmarkIT(clientAdapter, serverAdapter)
