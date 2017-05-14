package co.there4.hexagon

import co.there4.hexagon.store.MongoIdRepository
import co.there4.hexagon.store.mongoCollection
import co.there4.hexagon.store.mongoDatabase
import co.there4.hexagon.settings.SettingsManager.settings
import java.lang.System.getenv
import kotlin.reflect.KProperty1

internal const val DB_ROWS = 10000

private val DB_HOST = getenv("DBHOST") ?: "localhost"
private val DB = settings["database"] as? String ?: "hello_world"
private val WORLD: String = settings["worldCollection"] as? String ?: "world"
private val FORTUNE: String = settings["fortuneCollection"] as? String ?: "fortune"

internal fun createStore(engine: String): Repository = when (engine) {
    "mongodb" -> MongoDbRepository()
    else -> error("Unsupported database")
}

internal interface Repository {
    fun findFortunes(): List<Fortune>
    fun findWorlds(queries: Int): List<World>
    fun replaceWorlds(queries: Int): List<World>
}

internal class MongoDbRepository : Repository {
    private val database = mongoDatabase("mongodb://$DB_HOST/$DB")

    internal val worldRepository = repository(WORLD, World::_id)
    internal val fortuneRepository = repository(FORTUNE, Fortune::_id)

    // TODO Find out why it fails when creating index '_id' with background: true
    private inline fun <reified T : Any> repository(name: String, key: KProperty1<T, Int>) =
        MongoIdRepository(T::class, mongoCollection(name, database), key, indexOrder = null)

    override fun findFortunes() = fortuneRepository.findObjects().toList()

    override fun findWorlds(queries: Int) =
        (1..queries).map { worldRepository.find(rnd()) }.filterNotNull()

    override fun replaceWorlds(queries: Int) = (1..queries).map {
        val id = rnd()
        val newWorld = World(id, id)
        worldRepository.replaceObject(newWorld)
        newWorld
    }
}
