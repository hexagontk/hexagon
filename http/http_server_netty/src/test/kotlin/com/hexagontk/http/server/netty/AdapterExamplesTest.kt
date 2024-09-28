package com.hexagontk.http.server.netty

import com.hexagontk.http.client.HttpClientPort
import com.hexagontk.http.client.jetty.ws.JettyWsHttpClient
import com.hexagontk.http.server.HttpServerPort
import com.hexagontk.http.test.examples.*
import com.hexagontk.serialization.jackson.JacksonTextFormat
import com.hexagontk.serialization.jackson.json.Json
import com.hexagontk.serialization.jackson.yaml.Yaml
import org.junit.jupiter.api.condition.DisabledInNativeImage

// TODO Assert context methods (request.method, request.protocol...)
// TODO Check response headers don't contain invalid chars (\n, \t...)

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
internal class AdapterHttpsTest : HttpsTest(clientAdapter, serverAdapter)
internal class AdapterZipTest : ZipTest(clientAdapter, serverAdapter)
internal class AdapterCookiesTest : CookiesTest(clientAdapter, serverAdapter)
internal class AdapterFilesTest : FilesTest(clientAdapter, serverAdapter)
internal class AdapterMultipartTest : MultipartTest(clientAdapter, serverAdapter)
internal class AdapterCorsTest : CorsTest(clientAdapter, serverAdapter)
internal class AdapterSamplesTest : SamplesTest(clientAdapter, serverAdapter)
internal class AdapterMultipartSamplesTest : MultipartSamplesTest(clientAdapter, serverAdapter)
internal class AdapterBenchmarkIT : BenchmarkIT(clientAdapter, serverAdapter)
internal class AdapterSseTest : SseTest(clientAdapter, serverAdapter)
@DisabledInNativeImage // TODO Fix this
internal class AdapterWebSocketsTest : WebSocketsTest(clientAdapter, serverAdapter)

val liteServerAdapter: () -> HttpServerPort = {
    NettyHttpServer(
        keepAliveHandler = false,
        httpAggregatorHandler = false,
        chunkedHandler = false,
        enableWebsockets = false,
    )
}

internal class LiteAdapterBooksTest : BooksTest(clientAdapter, liteServerAdapter)
internal class LiteAdapterErrorsTest : ErrorsTest(clientAdapter, liteServerAdapter)
internal class LiteAdapterFiltersTest : FiltersTest(clientAdapter, liteServerAdapter)
internal class LiteAdapterHttpsTest : HttpsTest(clientAdapter, liteServerAdapter)
internal class LiteAdapterZipTest : ZipTest(clientAdapter, liteServerAdapter)
internal class LiteAdapterCookiesTest : CookiesTest(clientAdapter, liteServerAdapter)
internal class LiteAdapterCorsTest : CorsTest(clientAdapter, liteServerAdapter)
internal class LiteAdapterBenchmarkIT : BenchmarkIT(clientAdapter, liteServerAdapter)
internal class LiteAdapterSseTest : SseTest(clientAdapter, liteServerAdapter)
