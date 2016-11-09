package co.there4.hexagon

import co.there4.hexagon.repository.*
import co.there4.hexagon.rest.crud
import co.there4.hexagon.serialization.serialize
import co.there4.hexagon.settings.SettingsManager.setting
import co.there4.hexagon.web.*
import kotlinx.html.*

import java.lang.System.getenv
import java.net.InetAddress.getByName as address
import java.time.LocalDateTime.now
import java.util.concurrent.ThreadLocalRandom

import kotlin.reflect.KProperty1

internal data class Message(val message: String = "Hello, World!")
internal data class Fortune(val _id: Int, val message: String)
internal data class World(val _id: Int, val id: Int, val randomNumber: Int = rnd())

internal val DB_ROWS = 10000

private val CONTENT_TYPE_JSON = "application/json"
private val QUERIES_PARAM = "queries"

private val DB_HOST = getenv("DBHOST") ?: "localhost"
private val DB = setting<String>("database") ?: "hello_world"
private val WORLD: String = setting<String>("worldCollection") ?: "world"
private val FORTUNE: String = setting<String>("fortuneCollection") ?: "fortune"

private val database = mongoDatabase("mongodb://$DB_HOST/$DB")

internal val worldRepository = repository(WORLD, World::_id)
internal val fortuneRepository = repository(FORTUNE, Fortune::_id)

private val fortune = Fortune(0, "Additional fortune added at request time.")

private inline fun <reified T : Any> repository(name: String, key: KProperty1<T, Int>) =
    MongoIdRepository(T::class, mongoCollection(name, database), key)

private fun rnd() = ThreadLocalRandom.current().nextInt(DB_ROWS) + 1

private fun Exchange.hasQueryCount() = request[QUERIES_PARAM] == null

private fun Exchange.getDb() {
    val worlds = (1..getQueries()).map { worldRepository.find(rnd()) }.filterNotNull()

    ok(if (hasQueryCount()) worlds[0].serialize() else worlds.serialize(), CONTENT_TYPE_JSON)
}

private fun findFortune() =
    (fortuneRepository.findObjects().toList() + fortune).sortedBy { it.message }

private fun Exchange.getUpdates() {
    val worlds = (1..getQueries()).map {
        val id = rnd()
        val newWorld = World(id, id)
        worldRepository.replaceObject(newWorld)
        newWorld
    }

    ok(if (hasQueryCount()) worlds[0].serialize() else worlds.serialize(), CONTENT_TYPE_JSON)
}

private fun Exchange.getQueries() =
    try {
        val queries = request[QUERIES_PARAM]?.toInt() ?: 1
        when {
            queries < 1 -> 1
            queries > 500 -> 500
            else -> queries
        }
    }
    catch (ex: NumberFormatException) {
        1
    }

fun benchmarkRoutes() {
    before {
        response.addHeader("Server", "Servlet/3.1")
        response.addHeader("Transfer-Encoding", "chunked")
        response.addHeader("Date", httpDate(now()))
    }

    get("/plaintext") { ok("Hello, World!", "text/plain") }
    get("/json") { ok(Message().serialize(), CONTENT_TYPE_JSON) }
    get("/fortunes") { template("fortunes.html", "fortunes" to findFortune()) }
    get("/db") { getDb() }
    get("/query") { getDb() }
    get("/update") { getUpdates() }
}

fun main(args: Array<String>) {
    benchmarkRoutes()

    crud(worldRepository)
    crud(fortuneRepository)

    get("/fortunes_page") {
        page {
            html {
                head {
                    title { +"Fortunes" }
                }
                body {
                    table {
                        tr {
                            th { +"id" }
                            th { +"message" }
                        }
                        findFortune().forEach {
                            tr {
                                td { +it._id }
                                td { +it.message }
                            }
                        }
                    }
                }
            }
        }
    }

    run()
}
