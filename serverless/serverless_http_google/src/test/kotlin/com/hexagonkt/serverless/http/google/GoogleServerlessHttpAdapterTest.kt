package com.hexagonkt.serverless.http.google

import com.google.cloud.functions.invoker.runner.Invoker
import com.hexagonkt.core.freePort
import com.hexagonkt.core.urlOf
import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientSettings
import com.hexagonkt.http.client.java.JavaClientAdapter
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GoogleServerlessHttpAdapterTest {

    @Test fun `Google functions work ok`() {
        val port = freePort()
        val baseUrl = urlOf("http://localhost:${port}")
        val client = HttpClient(JavaClientAdapter(), HttpClientSettings(baseUrl))

        invoker(port, GoogleServerlessHttpAdapter::class)
        client.start()
        assertEquals("Hello World!", client.get().bodyString())
    }

    private fun invoker(port: Int, function: KClass<*>): Invoker {
        val target = function.qualifiedName
        val classLoader = ClassLoader.getSystemClassLoader()
        return Invoker(port, target, null, classLoader).apply { startTestServer() }
    }
}
