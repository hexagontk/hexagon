package com.hexagontk.serverless.http.google

import com.google.cloud.functions.HttpFunction
import com.google.cloud.functions.HttpRequest
import com.google.cloud.functions.HttpResponse
import com.google.cloud.functions.invoker.runner.Invoker
import com.hexagontk.core.freePort
import com.hexagontk.core.urlOf
import com.hexagontk.http.client.HttpClient
import com.hexagontk.http.client.HttpClientSettings
import com.hexagontk.http.client.java.JavaClientAdapter
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GoogleServerlessHttpAdapterTest {

    class TestServerlessHttpAdapter: HttpFunction {
        override fun service(request: HttpRequest, response: HttpResponse) {
            response.writer.write("Hello World!")
        }
    }

    @Test fun `Google functions work ok`() {
        val port = freePort()
        val baseUrl = urlOf("http://localhost:${port}")
        val client = HttpClient(JavaClientAdapter(), HttpClientSettings(baseUrl))

        invoker(port, TestServerlessHttpAdapter::class)
        client.start()
        assertEquals("Hello World!", client.get().bodyString())
    }

    private fun invoker(port: Int, function: KClass<*>): Invoker {
        val target = function.qualifiedName
        val classLoader = ClassLoader.getSystemClassLoader()
        return Invoker(port, target, null, classLoader).apply { startTestServer() }
    }
}
