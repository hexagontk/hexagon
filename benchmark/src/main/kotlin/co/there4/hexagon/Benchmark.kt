package co.there4.hexagon

import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.reflect.KClass

import co.there4.hexagon.ratpack.KContext
import co.there4.hexagon.rest.applicationStart
import co.there4.hexagon.serialization.serialize
import co.there4.hexagon.repository.MongoIdRepository
import co.there4.hexagon.repository.mongoCollection
import co.there4.hexagon.repository.mongoDatabase

import ratpack.server.BaseDir

import co.there4.hexagon.Benchmark.Companion.DB_ROWS
import java.time.LocalDateTime.now

internal class MongoDbRepository (settings: Properties) {
    val DATABASE = settings.getProperty ("mongodb.url")
    val WORLD = settings.getProperty ("mongodb.world.collection")
    val FORTUNE = settings.getProperty ("mongodb.fortune.collection")

    val database = mongoDatabase(DATABASE)
    val worldRepository = idRepository(World::class, WORLD, Int::class, { it.id })
    val fortuneRepository = idRepository(Fortune::class, FORTUNE, Int::class, { it.id })

    val random = ThreadLocalRandom.current ()

    fun <T : Any, K : Any> idRepository (
        type: KClass<T>, collectionName: String, keyType: KClass<K>, keySupplier: (T) -> K) =
            MongoIdRepository(
                type,
                mongoCollection (collectionName, database),
                "_id",
                keyType,
                keySupplier
            )

    fun rnd () = random.nextInt (DB_ROWS) + 1

    fun getFortunes (): List<Fortune> = fortuneRepository.findObjects ().toList()

    fun getWorlds (queries: Int, update: Boolean): Array<World> =
        Array(queries) {
            val id = rnd ()
            if (update) updateWorld (id, rnd()) else findWorld (id)
        }

    private fun findWorld (id: Int) = worldRepository.find(id)

    private fun updateWorld (id: Int, random: Int): World {
        val newWorld = World (id, random)
        worldRepository.replaceObject (newWorld)
        return newWorld
    }
}

internal data class Message (val message: String = "Hello, World!")
internal data class Fortune (val id: Int, val message: String)
internal data class World (val id: Int, val randomNumber: Int)

internal class Benchmark {
    companion object {
        val SETTINGS_RESOURCE = "/benchmark.properties"
        val DB_ROWS = 10000

        val MESSAGE = "Hello, World!"
        val CONTENT_TYPE_TEXT = "text/plain"
        val CONTENT_TYPE_JSON = "application/json"
        val QUERIES_PARAM = "queries"

        val repository = MongoDbRepository (loadConfiguration ())

        fun loadConfiguration (): Properties {
            try {
                val settings = Properties ()
                settings.load (Benchmark::class.java.getResourceAsStream (SETTINGS_RESOURCE))
                return settings
            }
            catch (ex: Exception) {
                throw RuntimeException (ex)
            }
        }
    }

    private fun KContext.getDb () {
        try {
            val worlds = repository.getWorlds (getQueries (), false)
            response.contentType (CONTENT_TYPE_JSON)
            ok ((if (request.queryParams [QUERIES_PARAM] == null) worlds[0] else worlds).serialize())
        }
        catch (e: Exception) {
            e.printStackTrace ()
            halt (e.message ?: "")
        }
    }

    private fun KContext.getFortunes () {
        try {
            val fortune = Fortune (0, "Additional fortune added at request time.")
            val fortunes:List<Fortune> = repository.getFortunes () + fortune
            fortunes.sortedBy { it.message }

            response.contentType ("text/html; charset=utf-8")
            template ("fortunes.html", mapOf ("fortunes" to fortunes))
        }
        catch (e: Exception) {
            halt (e.message ?: "")
        }
    }

    private fun KContext.getUpdates () {
        try {
            val worlds = repository.getWorlds (getQueries (), true)
            response.contentType (CONTENT_TYPE_JSON)
            ok((if (request.queryParams [QUERIES_PARAM] == null) worlds[0] else worlds).serialize())
        }
        catch (e: Exception) {
            e.printStackTrace ()
            halt(e.message ?: "")
        }
    }

    private fun KContext.getQueries (): Int {
        try {
            val parameter = request.queryParams [QUERIES_PARAM]
            if (parameter == null)
                return 1

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

    private fun KContext.addCommonHeaders () {
        response.headers ["Server"] = "Ratpack/1.3"
        response.headers ["Date"] = httpDate (now())
    }

    init {
        applicationStart {
            serverConfig {
                val settings = loadConfiguration ()
                port(settings.getProperty ("web.port").toInt())
//                address(InetAddress.getByName(settings.getProperty ("web.host")))
                baseDir(BaseDir.find("fortunes.html"))
            }

            handlers {
                all {
                    addCommonHeaders()
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

fun main (args: Array<String>) {
    Benchmark ()
}
