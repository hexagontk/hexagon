package com.hexagontk.http.server.netty.epoll

import com.hexagontk.http.client.HttpClientPort
import com.hexagontk.http.client.jetty.JettyHttpClient
import com.hexagontk.http.server.HttpServerPort
import com.hexagontk.http.test.examples.*
import com.hexagontk.serialization.jackson.JacksonTextFormat
import com.hexagontk.serialization.jackson.json.Json
import com.hexagontk.serialization.jackson.yaml.Yaml
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS

// TODO Assert context methods (request.method, request.protocol...)
// TODO Check response headers don't contain invalid chars (\n, \t...)

val clientAdapter: () -> HttpClientPort = ::JettyHttpClient
val serverAdapter: () -> HttpServerPort = ::NettyEpollHttpServer
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
internal class AdapterClientCookiesTest : ClientCookiesTest(clientAdapter, serverAdapter, formats)
@EnabledOnOs(OS.LINUX)
internal class AdapterClientHttp2Test : ClientHttp2Test(clientAdapter, serverAdapter, formats)
@EnabledOnOs(OS.LINUX)
internal class AdapterClientHttpsTest : ClientHttpsTest(clientAdapter, serverAdapter, formats)
@EnabledOnOs(OS.LINUX)
internal class AdapterClientMultipartTest : ClientMultipartTest(clientAdapter, serverAdapter, formats)
@EnabledOnOs(OS.LINUX)
internal class AdapterHttpsTest : HttpsTest(clientAdapter, serverAdapter)
@EnabledOnOs(OS.LINUX)
internal class AdapterZipTest : ZipTest(clientAdapter, serverAdapter)
@EnabledOnOs(OS.LINUX)
internal class AdapterCookiesTest : CookiesTest(clientAdapter, serverAdapter)
@EnabledOnOs(OS.LINUX)
internal class AdapterFilesTest : FilesTest(clientAdapter, serverAdapter)
@EnabledOnOs(OS.LINUX)
internal class AdapterMultipartTest : MultipartTest(clientAdapter, serverAdapter)
@EnabledOnOs(OS.LINUX)
internal class AdapterCorsTest : CorsTest(clientAdapter, serverAdapter)
@EnabledOnOs(OS.LINUX)
internal class AdapterSamplesTest : SamplesTest(clientAdapter, serverAdapter)
@EnabledOnOs(OS.LINUX)
internal class AdapterMultipartSamplesTest : MultipartSamplesTest(clientAdapter, serverAdapter)
@EnabledOnOs(OS.LINUX)
internal class AdapterBenchmarkIT : BenchmarkIT(clientAdapter, serverAdapter)
@EnabledOnOs(OS.LINUX)
internal class AdapterSseTest : SseTest(clientAdapter, serverAdapter)
