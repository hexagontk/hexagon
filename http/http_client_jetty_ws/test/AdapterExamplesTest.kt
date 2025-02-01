package com.hexagontk.http.client.jetty.ws

import com.hexagontk.http.client.HttpClientPort
import com.hexagontk.http.server.HttpServerPort
import com.hexagontk.http.server.netty.NettyHttpServer
import com.hexagontk.http.test.examples.*
import com.hexagontk.serialization.jackson.JacksonTextFormat
import com.hexagontk.serialization.jackson.json.Json
import com.hexagontk.serialization.jackson.yaml.Yaml
import org.junit.jupiter.api.condition.DisabledInNativeImage

val clientAdapter: () -> HttpClientPort = ::JettyWsHttpClient
val serverAdapter: () -> HttpServerPort = ::NettyHttpServer
val formats: List<JacksonTextFormat> = listOf(Json, Yaml)

internal class AdapterBooksTest : BooksTest(clientAdapter, serverAdapter)
internal class AdapterErrorsTest : ErrorsTest(clientAdapter, serverAdapter)
internal class AdapterFiltersTest : FiltersTest(clientAdapter, serverAdapter)
internal class AdapterClientTest : ClientTest(clientAdapter, serverAdapter, formats)
internal class AdapterClientCookiesTest : ClientCookiesTest(clientAdapter, serverAdapter, formats)
internal class AdapterClientHttp2Test : ClientHttp2Test(clientAdapter, serverAdapter, formats)
internal class AdapterClientHttpsTest : ClientHttpsTest(clientAdapter, serverAdapter, formats)
internal class AdapterClientMultipartTest : ClientMultipartTest(clientAdapter, serverAdapter, formats)
internal class AdapterHttp2Test : Http2Test(clientAdapter, serverAdapter)
internal class AdapterHttpsTest : HttpsTest(clientAdapter, serverAdapter)
internal class AdapterZipTest : ZipTest(clientAdapter, serverAdapter)
internal class AdapterCookiesTest : CookiesTest(clientAdapter, serverAdapter)
internal class AdapterMultipartTest : MultipartTest(clientAdapter, serverAdapter)
internal class AdapterMultipartSamplesTest : MultipartSamplesTest(clientAdapter, serverAdapter)
internal class AdapterFilesTest : FilesTest(clientAdapter, serverAdapter)
internal class AdapterCorsTest : CorsTest(clientAdapter, serverAdapter)
internal class AdapterSamplesTest : SamplesTest(clientAdapter, serverAdapter)
internal class AdapterSseTest : SseTest(clientAdapter, serverAdapter)
@DisabledInNativeImage
internal class AdapterWebSocketsTest : WebSocketsTest(clientAdapter, serverAdapter)
