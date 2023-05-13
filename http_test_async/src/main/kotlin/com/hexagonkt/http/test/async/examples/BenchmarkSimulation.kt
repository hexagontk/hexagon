package com.hexagonkt.http.test.examples.async

import io.gatling.javaapi.core.CoreDsl
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl

class BenchmarkSimulation: Simulation() {

    private val protocol = System.getProperty("protocol") ?: "http"
    private val host = System.getProperty("host") ?: "localhost"
    private val port = System.getProperty("port") ?: 0

    private val times = System.getProperty("times")?.toInt() ?: 256
    private val users = System.getProperty("users")?.toInt() ?: 64

    private val http = HttpDsl.http.baseUrl("$protocol://$host:$port")
    private val population = CoreDsl.rampUsers(users).during(5)

    private val getBooks = CoreDsl.repeat(times).on(CoreDsl.exec(HttpDsl.http("get /a/books").get("/a/books")))

    private val scenario = CoreDsl.scenario("Get Books").exec(getBooks)

    init {
        setUp(scenario.injectOpen(population))
            .protocols(http)
            .assertions(CoreDsl.global().successfulRequests().percent().gte(100.0))
    }
}
