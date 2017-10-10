package com.hexagonkt

import com.hexagonkt.serialization.parse
import com.hexagonkt.client.Client
import com.hexagonkt.serialization.parseList
import com.hexagonkt.server.HttpMethod.GET
import org.asynchttpclient.Response
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.lang.System.setProperty
import kotlin.test.assertFailsWith

internal const val THREADS = 4
internal const val TIMES = 2

class BenchmarkJettyMongoDbTest : BenchmarkTest("jetty", "mongodb")
class BenchmarkJettyPostgreSqlTest : BenchmarkTest("jetty", "postgresql")
class BenchmarkJettyPostgreSqlRockerTest : BenchmarkTest("jetty", "postgresql", "rocker")

class BenchmarkUndertowMongoDbTest : BenchmarkTest("undertow", "mongodb")
class BenchmarkUndertowPostgreSqlTest : BenchmarkTest("undertow", "postgresql")

@Test(threadPoolSize = THREADS, invocationCount = TIMES)
@Suppress("MemberVisibilityCanPrivate")
abstract class BenchmarkTest(
    private val webEngine: String,
    private val databaseEngine: String,
    private val templateEngine: String = "pebble"
) {
    private val client by lazy { Client("http://localhost:${benchmarkServer?.runtimePort}") }

    @BeforeClass fun warmup() {
        setProperty("DBSTORE", databaseEngine)
        setProperty("WEBENGINE", webEngine)
        setProperty("TEMPLATE_ENGINE", templateEngine)
        main()

        @Suppress("ConstantConditionIf")
        val warmupRounds = if (THREADS > 1) 2 else 0
        (1..warmupRounds).forEach {
            json()
            plaintext()
            `no query parameter`()
            `empty query parameter`()
            `text query parameter`()
            `zero queries`()
            `one thousand queries`()
            `one query`()
            `ten queries`()
            `one hundred queries`()
            `five hundred queries`()
            fortunes()
            `no updates parameter`()
            `empty updates parameter`()
            `text updates parameter`()
            `zero updates`()
            `one thousand updates`()
            `one update`()
            `ten updates`()
            `one hundred updates`()
            `five hundred updates`()
        }
    }

    @AfterClass fun cooldown() {
        benchmarkStore?.close()
        benchmarkServer?.stop()
    }

    fun store() {
        assertFailsWith<IllegalStateException> {
            createStore("invalid")
        }
    }

    fun web() {
        val web = Web()

        val webRoutes = web.serverRouter.requestHandlers
            .map { it.route.methods.first() to it.route.path.path }

        val benchmarkRoutes = listOf(
            GET to "/plaintext",
            GET to "/json",
            GET to "/fortunes",
            GET to "/db",
            GET to "/query",
            GET to "/update"
        )

        assert(webRoutes.containsAll(benchmarkRoutes))
    }

    fun json() {
        val response = client.get("/json")
        val content = response.responseBody

        checkResponse(response, "application/json")
        assert("Hello, World!" == content.parse(Message::class).message)
    }

    fun plaintext() {
        val response = client.get("/plaintext")
        val content = response.responseBody

        checkResponse(response, "text/plain")
        assert("Hello, World!" == content)
    }

    fun fortunes() {
        val response = client.get("/fortunes")
        val content = response.responseBody

        checkResponse(response, "text/html;charset=utf-8")
        assert(content.contains("<td>&lt;script&gt;alert(&quot;This should not be "))
        assert(content.contains(" displayed in a browser alert box.&quot;);&lt;/script&gt;</td>"))
        assert(content.contains("<td>フレームワークのベンチマーク</td>"))
    }

    fun `no query parameter`() {
        val response = client.get("/db")
        val body = response.responseBody

        checkResponse(response, "application/json")
        val bodyMap = body.parse(Map::class)
        assert(bodyMap.containsKey(World::id.name))
        assert(bodyMap.containsKey(World::randomNumber.name))
    }

    fun `no updates parameter`() {
        val response = client.get("/update")
        val body = response.responseBody

        checkResponse(response, "application/json")
        val bodyMap = body.parseList(Map::class).first()
        assert(bodyMap.containsKey(World::id.name))
        assert(bodyMap.containsKey(World::randomNumber.name))
    }

    fun `empty query parameter`() = checkDbRequest("/query?queries", 1)
    fun `text query parameter`() = checkDbRequest("/query?queries=text", 1)
    fun `zero queries`() = checkDbRequest("/query?queries=0", 1)
    fun `one thousand queries`() = checkDbRequest("/query?queries=1000", 500)
    fun `one query`() = checkDbRequest("/query?queries=1", 1)
    fun `ten queries`() = checkDbRequest("/query?queries=10", 10)
    fun `one hundred queries`() = checkDbRequest("/query?queries=100", 100)
    fun `five hundred queries`() = checkDbRequest("/query?queries=500", 500)

    fun `empty updates parameter`() = checkDbRequest("/update?queries", 1)
    fun `text updates parameter`() = checkDbRequest("/update?queries=text", 1)
    fun `zero updates`() = checkDbRequest("/update?queries=0", 1)
    fun `one thousand updates`() = checkDbRequest("/update?queries=1000", 500)
    fun `one update`() = checkDbRequest("/update?queries=1", 1)
    fun `ten updates`() = checkDbRequest("/update?queries=10", 10)
    fun `one hundred updates`() = checkDbRequest("/update?queries=100", 100)
    fun `five hundred updates`() = checkDbRequest("/update?queries=500", 500)

    private fun checkDbRequest(path: String, itemsCount: Int) {
        val response = client.get(path)
        val content = response.responseBody

        checkResponse(response, "application/json")

        val resultsList = content.parse(List::class)
        assert(itemsCount == resultsList.size)

        (1..itemsCount).forEach {
            val r = resultsList[it - 1] as Map<*, *>
            assert(r.containsKey(World::id.name) && r.containsKey(World::randomNumber.name))
            assert(!r.containsKey(World::_id.name))
            assert((r[World::id.name] as Int) in 1..10000)
        }
    }

    private fun checkResponse(res: Response, contentType: String) {
        assert(res.headers ["Date"] != null)
        assert(res.headers ["Server"] != null)
        assert(res.headers ["Transfer-Encoding"] != null)
        assert(res.headers ["Content-Type"] == contentType)
    }
}
