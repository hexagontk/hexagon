package com.hexagontk.http.server.netty.io.uring

import com.hexagontk.http.HttpFeature.COOKIES
import com.hexagontk.http.HttpFeature.MULTIPART
import com.hexagontk.http.HttpFeature.SSE
import com.hexagontk.http.HttpFeature.WEBSOCKETS
import com.hexagontk.http.HttpFeature.ZIP
import com.hexagontk.http.client.HttpClientPort
import com.hexagontk.http.client.jetty.JettyHttpClient
import com.hexagontk.http.server.HttpServerPort
import com.hexagontk.http.test.examples.*
import com.hexagontk.http.test.examples.examples.ServerTest
import com.hexagontk.serialization.jackson.JacksonTextFormat
import com.hexagontk.serialization.jackson.json.Json
import com.hexagontk.serialization.jackson.yaml.Yaml
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS

// TODO Assert context methods (request.method, request.protocol...)
// TODO Check response headers don't contain invalid chars (\n, \t...)

val clientAdapter: () -> HttpClientPort = ::JettyHttpClient
val serverAdapter: () -> HttpServerPort = ::NettyIoUringHttpServer
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
internal class AdapterHttp2Test : Http2Test(clientAdapter, serverAdapter)
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
internal class AdapterSseTest : SseTest(clientAdapter, serverAdapter)
@EnabledOnOs(OS.LINUX)
internal class AdapterServerTest : ServerTest(
    clientAdapter,
    serverAdapter,
    options = setOf(
        "bossGroupThreads",
        "workerGroupThreads",
        "executor",
        "soBacklog",
        "soKeepAlive",
        "shutdownQuietSeconds",
        "shutdownTimeoutSeconds",
        "keepAliveHandler",
        "httpAggregatorHandler",
        "chunkedHandler",
        "enableWebsockets",
    ),
    features = setOf(ZIP, COOKIES, MULTIPART, WEBSOCKETS, SSE)
)
