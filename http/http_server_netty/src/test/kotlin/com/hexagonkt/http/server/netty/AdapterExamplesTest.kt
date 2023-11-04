package com.hexagonkt.http.server.netty

import com.hexagonkt.http.client.jetty.ws.JettyWsClientAdapter
import com.hexagonkt.http.test.examples.*
import com.hexagonkt.serialization.jackson.JacksonTextFormat
import com.hexagonkt.serialization.jackson.json.Json
import com.hexagonkt.serialization.jackson.yaml.Yaml
import org.junit.jupiter.api.condition.DisabledInNativeImage
import org.junit.jupiter.api.condition.DisabledOnOs
import org.junit.jupiter.api.condition.OS.WINDOWS

// TODO Assert context methods (request.method, request.protocol...)
// TODO Check response headers don't contain invalid chars (\n, \t...)

val clientAdapter: () -> JettyWsClientAdapter = ::JettyWsClientAdapter
val serverAdapter: () -> NettyServerAdapter = ::NettyServerAdapter
val formats: List<JacksonTextFormat> = listOf(Json, Yaml)

internal class AdapterBooksTest : BooksTest(clientAdapter, serverAdapter)
internal class AdapterErrorsTest : ErrorsTest(clientAdapter, serverAdapter)
internal class AdapterFiltersTest : FiltersTest(clientAdapter, serverAdapter)
internal class AdapterClientTest : ClientTest(clientAdapter, serverAdapter, formats)
@DisabledOnOs(WINDOWS) // TODO Make this work on GitHub runners
internal class AdapterHttpsTest : HttpsTest(clientAdapter, serverAdapter)
internal class AdapterZipTest : ZipTest(clientAdapter, serverAdapter)
internal class AdapterCookiesTest : CookiesTest(clientAdapter, serverAdapter)
internal class AdapterFilesTest : FilesTest(clientAdapter, serverAdapter)
internal class AdapterCorsTest : CorsTest(clientAdapter, serverAdapter)
internal class AdapterSamplesTest : SamplesTest(clientAdapter, serverAdapter)
internal class AdapterBenchmarkIT : BenchmarkIT(clientAdapter, serverAdapter)
internal class AdapterSseTest : SseTest(clientAdapter, serverAdapter)
@DisabledInNativeImage // TODO Fix this
internal class AdapterWebSocketsTest : WebSocketsTest(clientAdapter, serverAdapter)

val liteServerAdapter: () -> NettyServerAdapter = {
    NettyServerAdapter(
        keepAliveHandler = false,
        httpAggregatorHandler = false,
        chunkedHandler = false,
        enableWebsockets = false,
    )
}

internal class LiteAdapterBooksTest : BooksTest(clientAdapter, liteServerAdapter)
internal class LiteAdapterErrorsTest : ErrorsTest(clientAdapter, liteServerAdapter)
internal class LiteAdapterFiltersTest : FiltersTest(clientAdapter, liteServerAdapter)
@DisabledOnOs(WINDOWS) // TODO Make this work on GitHub runners
internal class LiteAdapterHttpsTest : HttpsTest(clientAdapter, liteServerAdapter)
internal class LiteAdapterZipTest : ZipTest(clientAdapter, liteServerAdapter)
internal class LiteAdapterCookiesTest : CookiesTest(clientAdapter, liteServerAdapter)
internal class LiteAdapterCorsTest : CorsTest(clientAdapter, liteServerAdapter)
internal class LiteAdapterBenchmarkIT : BenchmarkIT(clientAdapter, liteServerAdapter)
internal class LiteAdapterSseTest : SseTest(clientAdapter, liteServerAdapter)
