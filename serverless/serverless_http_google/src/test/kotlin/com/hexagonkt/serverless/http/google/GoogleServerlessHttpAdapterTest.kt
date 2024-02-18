package com.hexagonkt.serverless.http.google

import com.google.cloud.functions.invoker.runner.Invoker
import com.hexagonkt.core.freePort
import com.hexagonkt.core.urlOf
import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientSettings
import com.hexagonkt.http.client.jetty.JettyClientAdapter
import kotlin.reflect.KClass
import kotlin.test.Test

internal class GoogleServerlessHttpAdapterTest {

    @Test fun `Google functions work ok`() {
        val port = freePort()
//        val invoker = invoker(port, GoogleServerlessHttpAdapter::class)
        val client = HttpClient(JettyClientAdapter(), HttpClientSettings(urlOf("http://localhost:${port}")))
    }

    private fun invoker(port: Int, function: KClass<*>): Invoker {
        val target = function.qualifiedName
        val classLoader = ClassLoader.getSystemClassLoader()
        val invoker = Invoker(port, target, null, classLoader)
//        invoker.startTestServer()
        invoker.startServer()
        return invoker
    }
}
