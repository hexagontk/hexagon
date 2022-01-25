package com.hexagonkt.http.test

import com.hexagonkt.http.test.examples.BooksTest
import io.gatling.app.Gatling
import io.gatling.core.config.GatlingPropertiesBuilder
import org.junit.jupiter.api.Test

import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl
import io.gatling.javaapi.http.HttpDsl.*
import kotlin.test.assertEquals

class BenchmarkSimulation: Simulation() {

    private val protocol = System.getProperty("protocol") ?: "http"
    private val host = System.getProperty("host") ?: "localhost"
    private val port = System.getProperty("port") ?: 0

    private val times = System.getProperty("times")?.toInt() ?: 1_024
    private val users = System.getProperty("users")?.toInt() ?: 128

    private val http = HttpDsl.http.baseUrl("$protocol://$host:$port")
    private val population = rampUsers(users).during(5)

    private val getBooks = repeat(times).on(exec(http("get /a/books").get("/a/books")))

    private val scenario = scenario("Get Books").exec(getBooks)

    init {
        setUp(scenario.injectOpen(population))
            .protocols(http)
            .assertions(global().successfulRequests().percent().gte(100.0))
    }
}

internal class BenchmarkIT : BooksTest(clientAdapter, serverAdapter) {//, async) {

    @Test fun `Example benchmark`() {
        val runtimePort = server.runtimePort

        System.setProperty("port", runtimePort.toString())
        val properties = GatlingPropertiesBuilder()

        properties.simulationClass(BenchmarkSimulation::class.qualifiedName)
        properties.resultsDirectory(System.getProperty("buildDir") ?: "build")

        assertEquals(0, Gatling.fromMap(properties.build()))
    }
}
