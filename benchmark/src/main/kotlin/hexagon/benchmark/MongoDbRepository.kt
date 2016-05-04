package hexagon.benchmark

import com.mongodb.MongoClient
import java.util.*

import com.mongodb.client.model.Filters.eq
import java.lang.Integer.parseInt

import java.util.concurrent.ThreadLocalRandom

import com.mongodb.client.MongoCollection
import hexagon.benchmark.Application.Companion.DB_ROWS
import org.bson.Document

internal class MongoDbRepository (settings: Properties) {
    var worldCollection: MongoCollection<Document>
    var fortuneCollection: MongoCollection<Document>

    init {
        val PORT = parseInt (settings.getProperty ("mongodb.port"))
        val HOST = settings.getProperty ("mongodb.host")
        val DATABASE = settings.getProperty ("mongodb.database")
        val WORLD = settings.getProperty ("mongodb.world.collection")
        val FORTUNE = settings.getProperty ("mongodb.fortune.collection")

        val mongoClient = MongoClient (HOST, PORT)
        val db = mongoClient.getDatabase (DATABASE)
        worldCollection = db.getCollection (WORLD)
        fortuneCollection = db.getCollection (FORTUNE)
    }

    fun getFortunes (): List<Fortune> {
        val fortunes = mutableListOf<Fortune>()

        fortuneCollection.find ().forEach {
            fortunes.add (
                Fortune (
                    it.get ("_id", Number::class.java).toInt(),
                    it.get ("message").toString()
                )
            )
        }

        return fortunes
    }

    fun getWorlds (queries: Int, update: Boolean): Array<World> {
        val random = ThreadLocalRandom.current ()
        return Array(queries) {
            val id = random.nextInt (DB_ROWS) + 1
            if (update) updateWorld (id, random.nextInt (DB_ROWS) + 1) else findWorld (id)
        }
    }

    private fun findWorld (id: Int): World {
        return createWorld (worldCollection.find(eq ("_id", id)).first ())
    }

    private fun createWorld (world: Document): World {
        return World (
            world.get ("_id", Number::class.java).toInt(),
            world.get ("randomNumber", Number::class.java).toInt ()
        )
    }

    private fun updateWorld (id: Int, random: Int): World {
        val newWorld = Document ("_id", id).append ("randomNumber", random)
        worldCollection.replaceOne (eq ("_id", id), newWorld)

        return World (id, random)
    }
}
