package co.there4.hexagon

import co.there4.hexagon.serialization.convertToMap
import co.there4.hexagon.serialization.serialize
import co.there4.hexagon.web.*
import co.there4.hexagon.web.servlet.ServletServer

import java.net.InetAddress.getByName as address
import java.time.LocalDateTime.now
import java.util.concurrent.ThreadLocalRandom
import javax.servlet.annotation.WebListener

// DATA CLASSES
internal data class Message(val message: String = "Hello, World!")
internal data class Fortune(val _id: Int, val message: String)
internal data class World(val _id: Int, val id: Int = _id, val randomNumber: Int = rnd())

// CONSTANTS
private val CONTENT_TYPE_JSON = "application/json"
private val QUERIES_PARAM = "queries"

// UTILITIES
internal fun rnd() = ThreadLocalRandom.current().nextInt(DB_ROWS) + 1

private fun Exchange.returnWorlds(worlds: List<World>) {
    fun World.strip(): Map<*, *> = this.convertToMap().filterKeys { it != "_id" }

    val result =
        if (request[QUERIES_PARAM] == null) worlds[0].strip().serialize()
        else worlds.map(World::strip).serialize()

    ok(result, CONTENT_TYPE_JSON)
}

// HANDLERS
private fun Exchange.listFortunes() {
    val fortunes = findFortunes() + Fortune(0, "Additional fortune added at request time.")

    template("fortunes.html", "fortunes" to fortunes.sortedBy { it.message })
}

private fun Exchange.getDb() {
    val worlds = (1..getQueries()).map { findWorld() }.filterNotNull()

    returnWorlds(worlds)
}

private fun Exchange.getUpdates() {
    val worlds = (1..getQueries()).map {
        val id = rnd()
        val newWorld = World(id, id)
        replaceWorld(newWorld)
        newWorld
    }

    returnWorlds(worlds)
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

fun benchmarkRoutes(srv: Router = server) {
    srv.before {
        response.addHeader("Server", "Servlet/3.1")
        response.addHeader("Transfer-Encoding", "chunked")
        response.addHeader("Date", httpDate(now()))
    }

    srv.get("/plaintext") { ok("Hello, World!", "text/plain") }
    srv.get("/json") { ok(Message().serialize(), CONTENT_TYPE_JSON) }
    srv.get("/fortunes") { listFortunes() }
    srv.get("/db") { getDb() }
    srv.get("/query") { getDb() }
    srv.get("/update") { getUpdates() }
}

@WebListener class Web : ServletServer () {
    override fun init() {
        benchmarkRoutes(this)
    }
}

fun main(args: Array<String>) {
    initialize()
    benchmarkRoutes()
    run()
}
