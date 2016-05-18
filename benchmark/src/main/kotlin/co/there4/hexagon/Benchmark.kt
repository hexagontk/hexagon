package co.there4.hexagon

import java.util.concurrent.ThreadLocalRandom
import kotlin.reflect.KClass

import co.there4.hexagon.ratpack.KContext
import co.there4.hexagon.rest.applicationStart
import co.there4.hexagon.serialization.serialize
import co.there4.hexagon.repository.MongoIdRepository
import co.there4.hexagon.repository.mongoCollection
import co.there4.hexagon.repository.mongoDatabase

import ratpack.server.BaseDir

import co.there4.hexagon.configuration.ConfigManager
import java.lang.System.getenv
import java.net.InetAddress
import java.time.LocalDateTime

internal data class Message (val message: String = "Hello, World!")
internal data class Fortune (val _id: Int, val message: String)
internal data class World (val id: Int, val randomNumber: Int)

internal object Benchmark {
    private val DB_ROWS = 10000
    private val MESSAGE = "Hello, World!"
    private val CONTENT_TYPE_TEXT = "text/plain"
    private val CONTENT_TYPE_JSON = "application/json"
    private val QUERIES_PARAM = "queries"

    private val DATABASE = ConfigManager["mongodb.database"] as String
    private val WORLD = ConfigManager["mongodb.world.collection"] as String
    private val FORTUNE = ConfigManager["mongodb.fortune.collection"] as String

    private val database = mongoDatabase("mongodb://${getenv("DBHOST") ?: "localhost"}/$DATABASE")
    private val worldRepository = idRepository(World::class, WORLD, Int::class, { it.id })
    private val fortuneRepository = idRepository(Fortune::class, FORTUNE, Int::class, { it._id })

    private val random = ThreadLocalRandom.current ()

    private fun <T : Any, K : Any> idRepository (
        type: KClass<T>, collectionName: String, keyType: KClass<K>, keySupplier: (T) -> K) =
        MongoIdRepository(
            type,
            mongoCollection (collectionName, database),
            "_id",
            keyType,
            keySupplier
        )

    private fun rnd () = random.nextInt (DB_ROWS) + 1

    fun getWorlds (queries: Int, update: Boolean) = (1..queries).map {
        if (update) {
            val newWorld = World (rnd (), rnd())
            worldRepository.replaceObject (newWorld)
            newWorld
        }
        else {
            worldRepository.find(rnd ())
        }
    }

    private fun KContext.getDb () {
        val worlds = getWorlds (getQueries (), false)
        response.contentType (CONTENT_TYPE_JSON)
        ok (
            if (request.queryParams [QUERIES_PARAM] == null) worlds[0].serialize()
            else worlds.serialize()
        )
    }

    private fun KContext.getFortunes () {
        val fortune = Fortune (0, "Additional fortune added at request time.")
        val fortunes = fortuneRepository.findObjects ().toList() + fortune

        response.contentType ("text/html; charset=utf-8")
        template ("fortunes.html", mapOf ("fortunes" to fortunes.sortedBy { it.message }))
    }

    private fun KContext.getUpdates () {
        val worlds = getWorlds (getQueries (), true)
        response.contentType (CONTENT_TYPE_JSON)
        ok (
            if (request.queryParams [QUERIES_PARAM] == null) worlds[0].serialize()
            else worlds.serialize()
        )
    }

    private fun KContext.getQueries (): Int {
        try {
            val parameter = request.queryParams [QUERIES_PARAM] ?: return 1

            val queries = parameter.toInt()
            if (queries < 1)
                return 1
            if (queries > 500)
                return 500

            return queries
        }
        catch (ex: NumberFormatException) {
            return 1
        }
    }

    private fun KContext.getPlaintext () {
        response.contentType (CONTENT_TYPE_TEXT)
        ok(MESSAGE)
    }

    private fun KContext.getJson () {
        response.contentType (CONTENT_TYPE_JSON)
        ok(Message ().serialize())
    }

    fun start() {
        applicationStart {
            serverConfig {
                port((ConfigManager["web.port"] as String).toInt())
                address(InetAddress.getByName(ConfigManager["web.host"] as String))
                baseDir(BaseDir.find("benchmark.properties"))
                development(false)
            }

            handlers {
                all {
                    response.headers ["Server"] = "Ratpack/1.3"
                    response.headers ["Transfer-Encoding"] = "chunked"
                    response.headers ["Date"] = httpDate (LocalDateTime.now())
                    next()
                }
                get ("json") { getJson() }
                get ("db") { getDb() }
                get ("query") { getDb() }
                get ("fortune") { getFortunes() }
                get ("update") { getUpdates() }
                get ("plaintext") { getPlaintext() }
            }
        }
    }
}

fun main(args: Array<String>) {
    Benchmark.start()
}
