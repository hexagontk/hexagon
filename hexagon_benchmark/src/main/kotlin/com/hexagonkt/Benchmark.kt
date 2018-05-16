package com.hexagonkt

import com.hexagonkt.helpers.systemSetting
import com.hexagonkt.serialization.JsonFormat
import com.hexagonkt.serialization.convertToMap
import com.hexagonkt.serialization.serialize
import com.hexagonkt.server.*
import com.hexagonkt.server.jetty.JettyServletAdapter
import com.hexagonkt.server.servlet.ServletServer
import com.hexagonkt.settings.SettingsManager.settings
import com.hexagonkt.templates.TemplateManager.render
import com.hexagonkt.templates.TemplatePort
import com.hexagonkt.templates.pebble.PebbleAdapter
import com.hexagonkt.templates.rocker.RockerAdapter
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger

import java.util.*
import java.util.concurrent.ThreadLocalRandom
import javax.servlet.annotation.WebListener
import java.net.InetAddress.getByName as address

// DATA CLASSES
internal data class Message(val message: String)
internal data class Fortune(val _id: Int, val message: String)
internal data class World(val _id: Int, val id: Int, val randomNumber: Int)

// CONSTANTS
private const val TEXT_MESSAGE: String = "Hello, World!"
private const val QUERIES_PARAM = "queries"

private val contentTypeJson = JsonFormat.contentType
private val logger: Logger = getLogger("BENCHMARK_LOGGER")
private val defaultLocale: Locale = Locale.getDefault()
private val storageEngines = listOf("mongodb", "postgresql")
private val templateEngines = listOf("pebble", "rocker")

// UTILITIES
internal fun randomWorld() = ThreadLocalRandom.current().nextInt(WORLD_ROWS) + 1

private fun Call.returnWorlds(worldsList: List<World>) {
    val worlds = worldsList.map { it.convertToMap() - "_id" }
    ok(worlds.serialize(), contentTypeJson)
}

private fun Call.getWorldsCount() = (request[QUERIES_PARAM]?.toIntOrNull() ?: 1).let {
    when {
        it < 1 -> 1
        it > 500 -> 500
        else -> it
    }
}

// HANDLERS
private fun Call.listFortunes(store: Store, templateEngine: String): String {
    val templateEngineType = getTemplateEngine(templateEngine)
    val fortunes = store.findAllFortunes() + Fortune(0, "Additional fortune added at request time.")
    val sortedFortunes = fortunes.sortedBy { it.message }
    response.contentType = "text/html;charset=utf-8"
    return render(
        templateEngineType,
        "fortunes.$templateEngine.html",
        defaultLocale,
        mapOf("fortunes" to sortedFortunes)
    )
}

private fun Call.dbQuery(store: Store) {
    val world = store.findWorlds(1).first().convertToMap() - "_id"
    ok(world.serialize(), contentTypeJson)
}

private fun Call.getWorlds(store: Store) {
    returnWorlds(store.findWorlds(getWorldsCount()))
}

private fun Call.updateWorlds(store: Store) {
    returnWorlds(store.replaceWorlds(getWorldsCount()))
}

// CONTROLLER
private fun router(): Router = router {
    if (benchmarkStores == null) {
        error("Invalid Stores")
    }

    before {
        response.addHeader("Server", "Servlet/3.1")
        response.addHeader("Transfer-Encoding", "chunked")
        response.addHeader("Date", httpDate())
    }

    get("/plaintext") { ok(TEXT_MESSAGE, "text/plain") }
    get("/json") { ok(Message(TEXT_MESSAGE).serialize(), contentTypeJson) }
    benchmarkStores?.forEach({ (storeEngine, store) ->
        templateEngines.forEach({ templateEngine ->
            get("/$storeEngine/$templateEngine/fortunes") { listFortunes(store, templateEngine) }
        })

        get("/$storeEngine/db") { dbQuery(store) }
        get("/$storeEngine/query") { getWorlds(store) }
        get("/$storeEngine/update") { updateWorlds(store) }
    })
}

@WebListener class Web : ServletServer (router()) {
    init {
        if (benchmarkStores == null) {
            benchmarkStores = storageEngines.map { it to createStore(it) }.toMap()
        }
    }
}

fun getTemplateEngine(engine: String): TemplatePort = when (engine) {
    "pebble" -> PebbleAdapter
    "rocker" -> RockerAdapter
    else -> error("Unsupported template engine: $engine")
}

internal var benchmarkStores: Map<String, Store>? = null
internal var benchmarkServer: Server? = null

internal fun createEngine(engine: String): ServerPort = when (engine) {
    "jetty" -> JettyServletAdapter()
    else -> error("Unsupported server engine")
}

fun main(vararg args: String) {
    val engine = createEngine(systemSetting("WEBENGINE", "jetty"))
    benchmarkStores = storageEngines.map { it to createStore(it) }.toMap()

    logger.info("""
            Benchmark set up:
                - Engine: {}
                - Templates: {}
                - Stores: {}
        """.trimIndent(),
        engine.javaClass.name,
        templateEngines,
        storageEngines)

    benchmarkServer = Server(engine, settings, router()).apply { run() }
}
