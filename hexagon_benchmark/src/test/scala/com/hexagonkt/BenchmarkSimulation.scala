package com.hexagonkt

import java.lang.System.getProperty

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import io.gatling.http.request.builder.HttpRequestBuilder

class BenchmarkSimulation extends Simulation {
  private val host: String = property("host", "127.0.0.1")
  private val port: Int = property("port", "9020").toInt
  private val databaseEngine: String = property("databaseEngine", "mongodb")
  private val templateEngine: String = property("templateEngine", "pebble")
  private val users: Int = property("users", "256").toInt
  private val protocol: String = property("protocol", "http")
  private val count: Int = property("count", "32").toInt

  private val baseUrl: String = s"$protocol://$host:$port"
  private val httpConfiguration: HttpProtocolBuilder =
    http.baseURL(baseUrl).contentTypeHeader("text/plain")

  private val jsonRequest = get("json", "/json")
  private val plaintextRequest = get("plaintext", "/plaintext")
  private val dbRequest = get("db", s"/$databaseEngine/db")
  private val queryRequest = get("query", s"/$databaseEngine/query")
  private val queryBatchRequest = get("queryBatch", s"/$databaseEngine/query?queries=$count")
  private val updateRequest = get("update", s"/$databaseEngine/update")
  private val updateBatchRequest = get("updateBatch", s"/$databaseEngine/update?queries=$count")
  private val fortunesRequest = get("fortunes", s"/$databaseEngine/$templateEngine/fortunes")

  val checkScenario: ScenarioBuilder = scenario("CheckScenario")
    .exec(jsonRequest.check(regex(".*\"message\" : \"Hello, World!\".*")))
    .exec(plaintextRequest.check(regex(".*Hello, World!.*")))
    .exec(dbRequest.check(regex(".*\"id\".*")).check(regex(".*\"randomNumber\".*")))
    .exec(queryRequest.check(regex(".*\"id\".*")).check(regex(".*\"randomNumber\".*")))
    .exec(queryBatchRequest.check(regex(".*\"id\".*")).check(regex(".*\"randomNumber\".*")))
    .exec(updateRequest.check(regex(".*\"id\".*")).check(regex(".*\"randomNumber\".*")))
    .exec(updateBatchRequest.check(regex(".*\"id\".*")).check(regex(".*\"randomNumber\".*")))
    .exec(fortunesRequest
      .check(regex(".*&quot;This should not be displayed.*"))
      .check(regex(".*フレームワークのベンチマーク.*"))
    )

//  setUp(checkScenario.inject(atOnceUsers(1))).protocols(httpConfiguration)
  setUp(checkScenario.inject(atOnceUsers(users))).protocols(httpConfiguration)

  private def property(name: String, default: String): String =
    if (getProperty(name) != null) getProperty(name)
    else default

  private def get(name: String, url: String): HttpRequestBuilder = http(name + "Request").get(url)
}
