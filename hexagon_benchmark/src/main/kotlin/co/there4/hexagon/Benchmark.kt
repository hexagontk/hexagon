package co.there4.hexagon

import co.there4.hexagon.serialization.convertToMap
import co.there4.hexagon.serialization.serialize
import co.there4.hexagon.server.*
import co.there4.hexagon.server.engine.servlet.JettyServletEngine
import co.there4.hexagon.server.engine.servlet.ServletServer
import co.there4.hexagon.settings.SettingsManager.settings
import co.there4.hexagon.templates.pebble.PebbleEngine
import java.lang.System.*
import java.util.*

import java.net.InetAddress.getByName as address
import java.util.concurrent.ThreadLocalRandom
import javax.servlet.annotation.WebListener

// DATA CLASSES
internal data class Message(val message: String)
internal data class Fortune(val _id: Int, val message: String)
internal data class World(val _id: Int, val id: Int, val randomNumber: Int)

// CONSTANTS
private const val TEXT_MESSAGE: String = "Hello, World!"
private const val CONTENT_TYPE_JSON = "application/json"
private const val QUERIES_PARAM = "queries"

// UTILITIES
internal fun randomWorld() = ThreadLocalRandom.current().nextInt(WORLD_ROWS) + 1

private fun Call.returnWorlds(worldsList: List<World>) {
    val worlds = worldsList.map { it.convertToMap() - "_id" }
    val result = if (worlds.size == 1) worlds.first().serialize() else worlds.serialize()

    ok(result, CONTENT_TYPE_JSON)
}

private fun Call.getWorldsCount() = (request[QUERIES_PARAM]?.toIntOrNull() ?: 1).let {
    when {
        it < 1 -> 1
        it > 500 -> 500
        else -> it
    }
}

// HANDLERS
private fun Call.listFortunes(store: Store) {
    val fortunes = store.findAllFortunes() + Fortune(0, "Additional fortune added at request time.")
    val locale = Locale.getDefault()
    response.contentType = "text/html; charset=utf-8"
    template(PebbleEngine, "fortunes.html", locale, "fortunes" to fortunes.sortedBy { it.message })
}

private fun Call.getWorlds(store: Store) {
    returnWorlds(store.findWorlds(getWorldsCount()))
}

private fun Call.updateWorlds(store: Store) {
    returnWorlds(store.replaceWorlds(getWorldsCount()))
}

// CONTROLLER
private val router: Router by lazy {
    router {
        val store = createStore(getProperty("DBSTORE") ?: getenv("DBSTORE") ?: "mongodb")

        before {
            response.addHeader("Server", "Servlet/3.1")
            response.addHeader("Transfer-Encoding", "chunked")
            response.addHeader("Date", httpDate())
        }

        get("/plaintext") { ok(TEXT_MESSAGE, "text/plain") }
        get("/json") { ok(Message(TEXT_MESSAGE).serialize(), CONTENT_TYPE_JSON) }
        get("/fortunes") { listFortunes(store) }
        get("/db") { getWorlds(store) }
        get("/query") { getWorlds(store) }
        get("/update") { updateWorlds(store) }
    }
}

internal val server: Server by lazy { Server(JettyServletEngine(), settings, router) }

@WebListener class Web : ServletServer () {
    override fun createRouter() = router
}

fun main(vararg args: String) {
    if (args.isNotEmpty()) setProperty("DBSTORE", args.first())
    server.run()
}
