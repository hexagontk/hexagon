package com.hexagonkt.http.test.async.examples

import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.server.async.HttpServerPort
import com.hexagonkt.http.server.async.HttpServerSettings
import io.gatling.app.Gatling
import io.gatling.core.config.GatlingPropertiesBuilder
import org.junit.jupiter.api.Test

import kotlin.test.assertEquals

@Suppress("FunctionName") // This class's functions are intended to be used only in tests
abstract class BenchmarkIT(
    clientAdapter: () -> HttpClientPort,
    serverAdapter: () -> HttpServerPort,
    serverSettings: HttpServerSettings = HttpServerSettings(),
) : BooksTest(clientAdapter, serverAdapter, serverSettings) {

    @Test fun `Example benchmark`() {
        val runtimePort = server.runtimePort

        System.setProperty("port", runtimePort.toString())
        val properties = GatlingPropertiesBuilder()

        properties.simulationClass(BenchmarkSimulation::class.qualifiedName)
        properties.resultsDirectory(System.getProperty("buildDir") ?: "build")

        assertEquals(0, Gatling.fromMap(properties.build()))
    }
}
