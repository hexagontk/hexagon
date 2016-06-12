package co.there4.hexagon

import java.util.concurrent.ThreadLocalRandom

import co.there4.hexagon.serialization.serialize
import co.there4.hexagon.repository.MongoIdRepository
import co.there4.hexagon.repository.mongoCollection
import co.there4.hexagon.repository.mongoDatabase
import co.there4.hexagon.web.*
import co.there4.hexagon.web.jetty.JettyServer

import co.there4.hexagon.configuration.ConfigManager as Config
import java.lang.System.getenv
import java.net.InetAddress.getByName as address
import java.time.LocalDateTime.now

internal data class Message (val message: String = "Hello, World!")
internal data class Fortune (val _id: Int, val message: String)
internal data class World (val id: Int, val randomNumber: Int)

internal object Benchmark {
    private val DB_ROWS = 10000
    private val CONTENT_TYPE_JSON = "application/json"
    private val QUERIES_PARAM = "queries"

    private val DB: String = getenv("OPENSHIFT_APP_NAME") ?: Config["database"] as String? ?: "hello_world"
    private val WORLD: String = Config["worldCollection"] as String? ?: "world"
    private val FORTUNE: String = Config["fortuneCollection"] as String? ?: "fortune"

    private val DB_HOST = getenv("DBHOST") ?: "localhost"
    private val DB_PORT = getenv("OPENSHIFT_MONGODB_DB_PORT") ?: 27017
    private val DB_USER = getenv("OPENSHIFT_MONGODB_DB_USERNAME")
    private val DB_PASS = getenv("OPENSHIFT_MONGODB_DB_PASSWORD")

    private val database =
        if (DB_USER == null) mongoDatabase("mongodb://$DB_HOST:$DB_PORT/$DB")
        else mongoDatabase("mongodb://$DB_USER:$DB_PASS@$DB_HOST:$DB_PORT/$DB")

    private val worldRepository = MongoIdRepository(
        World::class, mongoCollection (WORLD, database), "_id", Int::class, { it.id }
    )

    private val fortuneRepository = MongoIdRepository(
        Fortune::class, mongoCollection (FORTUNE, database), "_id", Int::class, { it._id }
    )

    private fun rnd () = ThreadLocalRandom.current ().nextInt (DB_ROWS) + 1

    private fun Exchange.hasQueryCount() = request.parameters [QUERIES_PARAM] == null

    private fun Exchange.getDb () {
        val worlds = (1..getQueries()).map { worldRepository.find(rnd ()) }

        response.contentType = CONTENT_TYPE_JSON
        ok (if (hasQueryCount()) worlds[0].serialize() else worlds.serialize())
    }

    private fun Exchange.getFortunes () {
        val fortune = Fortune (0, "Additional fortune added at request time.")
        val fortunes = fortuneRepository.findObjects ().toList() + fortune

        response.contentType = "text/html; charset=utf-8"
        template ("fortunes.html", mapOf ("fortunes" to fortunes.sortedBy { it.message }))
    }

    private fun Exchange.getUpdates () {
        val worlds =  (1..getQueries()).map {
            val newWorld = World (rnd (), rnd())
            worldRepository.replaceObject (newWorld)
            newWorld
        }

        response.contentType = CONTENT_TYPE_JSON
        ok (if (hasQueryCount()) worlds[0].serialize() else worlds.serialize())
    }

    private fun Exchange.getQueries (): Int {
        try {
            val parameter = request.parameters[QUERIES_PARAM]?.first() ?: return 1

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

    private fun Exchange.getPlaintext () {
        response.contentType = "text/plain"
        ok("Hello, World!")
    }

    private fun Exchange.getJson () {
        response.contentType = CONTENT_TYPE_JSON
        ok(Message ().serialize())
    }

    fun start() {
        blacksheep = JettyServer (
            bindAddress = address(getenv("OPENSHIFT_DIY_IP") ?: Config["bindAddress"] as String? ?: "localhost"),
            bindPort = (getenv("OPENSHIFT_DIY_PORT") ?: Config["bindPort"] as String? ?: "5050").toInt()
        )

        before {
            response.addHeader("Server", "Ratpack/1.3")
            response.addHeader("Transfer-Encoding", "chunked")
            response.addHeader("Date", httpDate (now()))
        }

        get ("/json") { getJson() }
        get ("/db") { getDb() }
        get ("/query") { getDb() }
        get ("/fortune") { getFortunes() }
        get ("/update") { getUpdates() }
        get ("/plaintext") { getPlaintext() }

        run ()
    }
}

fun main(args: Array<String>) = Benchmark.start()
