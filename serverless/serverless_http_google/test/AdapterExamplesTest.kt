package com.hexagontk.serverless.http.google

import com.google.cloud.functions.HttpFunction
import com.google.cloud.functions.HttpRequest
import com.google.cloud.functions.HttpResponse
import com.google.cloud.functions.invoker.runner.Invoker
import com.hexagontk.core.freePort
import com.hexagontk.http.HttpFeature
import com.hexagontk.http.client.HttpClientPort
import com.hexagontk.http.client.jdk.JdkHttpClient
import com.hexagontk.http.model.HttpProtocol
import com.hexagontk.http.model.HttpProtocol.HTTP
import com.hexagontk.http.server.HttpServer
import com.hexagontk.http.server.HttpServerPort
import com.hexagontk.http.test.examples.*
import com.hexagontk.serialization.SerializationFormat
import com.hexagontk.serialization.jackson.json.Json
import com.hexagontk.serialization.jackson.yaml.Yaml

lateinit var delegate: HttpFunction

class ServerlessHttpAdapter: HttpFunction {

    override fun service(p0: HttpRequest?, p1: HttpResponse?) {
        delegate.service(p0, p1)
    }
}

val clientAdapter: () -> HttpClientPort = ::JdkHttpClient
val serverAdapter: () -> HttpServerPort = {
    object : HttpServerPort {
        var port: Int = 0
        var started = false

        lateinit var invoker: Invoker

        override fun runtimePort(): Int =
            port

        override fun started(): Boolean =
            started

        override fun startUp(server: HttpServer) {
            delegate = GoogleHttpFunction(server.handler)

            val target = ServerlessHttpAdapter::class.qualifiedName
            val classLoader = ClassLoader.getSystemClassLoader()

            port = freePort()
            invoker = Invoker(port, target, null, classLoader)
            invoker.startTestServer()
            started = true
        }

        override fun shutDown() {
            invoker.stopServer()
            started = false
        }

        override fun supportedProtocols(): Set<HttpProtocol> =
            setOf(HTTP)

        override fun supportedFeatures(): Set<HttpFeature> =
            emptySet()

        override fun options(): Map<String, *> =
            emptyMap<String, Any>()
    }
}

val formats: List<SerializationFormat> = listOf(Json, Yaml)

internal class AdapterBooksTest : BooksTest(clientAdapter, serverAdapter)
internal class AdapterErrorsTest : ErrorsTest(clientAdapter, serverAdapter)
internal class AdapterFiltersTest : FiltersTest(clientAdapter, serverAdapter)
// TODO One test is failing because of Jackson dependency conflicts
//internal class AdapterClientTest : ClientTest(clientAdapter, serverAdapter, formats)
//internal class AdapterClientMultipartTest : ClientMultipartTest(clientAdapter, serverAdapter, formats)
internal class AdapterFilesTest : FilesTest(clientAdapter, serverAdapter)
//internal class AdapterMultipartTest : MultipartTest(clientAdapter, serverAdapter)
internal class AdapterCorsTest : CorsTest(clientAdapter, serverAdapter)
internal class AdapterSamplesTest : SamplesTest(clientAdapter, serverAdapter)
//internal class AdapterMultipartSamplesTest : MultipartSamplesTest(clientAdapter, serverAdapter)
