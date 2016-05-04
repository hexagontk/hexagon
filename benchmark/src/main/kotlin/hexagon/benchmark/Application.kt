package hexagon.benchmark

import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.reflect.KClass

import co.there4.hexagon.ratpack.KContext
import co.there4.hexagon.rest.applicationStart
import co.there4.hexagon.serialization.serialize
import co.there4.hexagon.repository.MongoIdRepository
import co.there4.hexagon.repository.mongoDatabase

import com.mongodb.client.model.Filters.eq
import ratpack.server.BaseDir

import hexagon.benchmark.Application.Companion.DB_ROWS

internal class MongoDbRepository (settings: Properties) {
    val DATABASE = settings.getProperty ("mongodb.url")
    val WORLD = settings.getProperty ("mongodb.world.collection")
    val FORTUNE = settings.getProperty ("mongodb.fortune.collection")

    val database = mongoDatabase(DATABASE)
    val worldRepository = idRepository(World::class, Int::class, { it.id })
    val fortuneRepository = idRepository(Fortune::class, Int::class, { it.id })

    val random = ThreadLocalRandom.current ()

    fun <T : Any, K : Any> idRepository (
        type: KClass<T>, keyType: KClass<K>, keySupplier: (T) -> K) =
        MongoIdRepository(type, database, "_id", keyType, keySupplier)

    fun getFortunes (): List<Fortune> = fortuneRepository.findObjects ().toList()

    fun getWorlds (queries: Int, update: Boolean): Array<World> =
        Array(queries) {
            val id = random.nextInt (DB_ROWS) + 1
            if (update) updateWorld (id, random.nextInt (DB_ROWS) + 1) else findWorld (id)
        }

    private fun findWorld (id: Int) = worldRepository.find(id)

    private fun updateWorld (id: Int, random: Int): World {
        val newWorld = World (id, random)
        worldRepository.replaceOneObject (eq ("_id", id), newWorld)
        return newWorld
    }
}

internal data class Message (val message: String = "Hello, World!")
internal data class Fortune (val id: Int, val message: String)
internal data class World (val id: Int, val randomNumber: Int)

internal class Application {
    companion object {
        val SETTINGS_RESOURCE = "/server.properties"
        val DB_ROWS = 10000

        val MESSAGE = "Hello, World!"
        val CONTENT_TYPE_TEXT = "text/plain"
        val CONTENT_TYPE_JSON = "application/json"
        val QUERIES_PARAM = "queries"

        val repository = MongoDbRepository (loadConfiguration ())

        fun loadConfiguration (): Properties {
            try {
                val settings = Properties ()
                settings.load (Application::class.java.getResourceAsStream (SETTINGS_RESOURCE))
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

//    private fun KContext.addCommonHeaders () {
//        response.headers ["Server"] = "Undertow/1.1.2"
//        response.addDateHeader ("Date", Date ().getTime ())
//    }

    init {
        applicationStart {
            serverConfig {
                val settings = loadConfiguration ()
                port(settings.getProperty ("web.port").toInt())
//                bind (settings.getProperty ("web.host"))
                baseDir(BaseDir.find("fortunes.html"))
            }

            handlers {
                get ("/json") { getJson() }
                get ("/db") { getDb() }
                get ("/query") { getDb() }
                get ("/fortune") { getFortunes() }
                get ("/update") { getUpdates() }
                get ("/plaintext") { getPlaintext() }
                //        after (this::addCommonHeaders);
            }
        }
    }
}

fun main (args: Array<String>) {
    Application ()
}
