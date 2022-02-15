package com.hexagonkt.http.server.netty

import com.hexagonkt.http.client.jetty.JettyClientAdapter
import com.hexagonkt.http.server.HttpServerFeature.ASYNC
import com.hexagonkt.http.server.HttpServerSettings
import com.hexagonkt.http.test.examples.*
import com.hexagonkt.serialization.jackson.JacksonTextFormat
import com.hexagonkt.serialization.jackson.json.Json
import com.hexagonkt.serialization.jackson.yaml.Yaml

// TODO Fix disabled test

val clientAdapter: () -> JettyClientAdapter = ::JettyClientAdapter
val serverAdapter: () -> NettyServerAdapter = ::NettyServerAdapter
val formats: List<JacksonTextFormat> = listOf(Json, Yaml)
val async: HttpServerSettings = HttpServerSettings(features = setOf(ASYNC))

internal class AdapterBooksTest : BooksTest(clientAdapter, serverAdapter)
internal class AdapterErrorsTest : ErrorsTest(clientAdapter, serverAdapter)
internal class AdapterFiltersTest : FiltersTest(clientAdapter, serverAdapter)
//internal class AdapterClientTest : ClientTest(clientAdapter, serverAdapter, formats)
internal class AdapterHttpsTest : HttpsTest(clientAdapter, serverAdapter)
//internal class AdapterZipTest : ZipTest(clientAdapter, serverAdapter)
//internal class AdapterCookiesTest : CookiesTest(clientAdapter, serverAdapter)
//internal class AdapterFilesTest : FilesTest(clientAdapter, serverAdapter)
internal class AdapterCorsTest : CorsTest(clientAdapter, serverAdapter)
//internal class AdapterSamplesTest : SamplesTest(clientAdapter, serverAdapter)
internal class AdapterBenchmarkIT : BenchmarkIT(clientAdapter, serverAdapter)
//@Disabled
//internal class AdapterAsyncSseTest : SseTest(clientAdapter, serverAdapter, async)
