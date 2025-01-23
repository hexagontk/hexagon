package com.hexagontk.serverless.http.google

import com.google.cloud.functions.HttpFunction
import com.google.cloud.functions.invoker.runner.Invoker
import com.hexagontk.core.freePort
import com.hexagontk.http.client.HttpClient
import com.hexagontk.http.client.HttpClientSettings
import com.hexagontk.http.client.jdk.JdkHttpClient
import com.hexagontk.http.handlers.Get
import java.net.URI
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GoogleHttpFunctionTest {

    class TestServerlessHttpAdapter: HttpFunction by GoogleHttpFunction(
        Get { ok("Hello World!") }
    )

    @Test fun `Google functions work ok`() {
        val client = invoker(TestServerlessHttpAdapter::class)
        assertEquals("Hello World!", client.get().bodyString())
    }

    private fun invoker(function: KClass<*>): HttpClient {
        val port = freePort()
        val baseUri = URI("http://localhost:${port}")
        val client = HttpClient(JdkHttpClient(), HttpClientSettings(baseUri))

        invoker(port, function)
        client.start()
        return client
    }

    private fun invoker(port: Int, function: KClass<*>): Invoker {
        val target = function.qualifiedName
        val classLoader = ClassLoader.getSystemClassLoader()
        return Invoker(port, target, null, classLoader).apply { startTestServer() }
    }
}
