package com.hexagonkt.http.server.netty.epoll

import com.hexagonkt.http.client.jetty.JettyClientAdapter
import com.hexagonkt.http.test.examples.*
import com.hexagonkt.serialization.jackson.JacksonTextFormat
import com.hexagonkt.serialization.jackson.json.Json
import com.hexagonkt.serialization.jackson.yaml.Yaml
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS

// TODO Assert context methods (request.method, request.protocol...)
// TODO Check response headers don't contain invalid chars (\n, \t...)

val clientAdapter: () -> JettyClientAdapter = ::JettyClientAdapter
val serverAdapter: () -> NettyEpollServerAdapter = ::NettyEpollServerAdapter
val formats: List<JacksonTextFormat> = listOf(Json, Yaml)

@EnabledOnOs(OS.LINUX)
internal class AdapterBooksTest : BooksTest(clientAdapter, serverAdapter)
@EnabledOnOs(OS.LINUX)
internal class AdapterErrorsTest : ErrorsTest(clientAdapter, serverAdapter)
@EnabledOnOs(OS.LINUX)
internal class AdapterFiltersTest : FiltersTest(clientAdapter, serverAdapter)
@EnabledOnOs(OS.LINUX)
internal class AdapterClientTest : ClientTest(clientAdapter, serverAdapter, formats)
@EnabledOnOs(OS.LINUX)
internal class AdapterHttpsTest : HttpsTest(clientAdapter, serverAdapter)
@EnabledOnOs(OS.LINUX)
internal class AdapterZipTest : ZipTest(clientAdapter, serverAdapter)
@EnabledOnOs(OS.LINUX)
internal class AdapterCookiesTest : CookiesTest(clientAdapter, serverAdapter)
@EnabledOnOs(OS.LINUX)
internal class AdapterFilesTest : FilesTest(clientAdapter, serverAdapter)
@EnabledOnOs(OS.LINUX)
internal class AdapterCorsTest : CorsTest(clientAdapter, serverAdapter)
@EnabledOnOs(OS.LINUX)
internal class AdapterSamplesTest : SamplesTest(clientAdapter, serverAdapter)
@EnabledOnOs(OS.LINUX)
internal class AdapterBenchmarkIT : BenchmarkIT(clientAdapter, serverAdapter)
@EnabledOnOs(OS.LINUX)
internal class AdapterSseTest : SseTest(clientAdapter, serverAdapter)
