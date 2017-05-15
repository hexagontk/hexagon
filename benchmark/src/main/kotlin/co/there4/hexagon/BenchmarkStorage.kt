package co.there4.hexagon

import co.there4.hexagon.store.MongoIdRepository
import co.there4.hexagon.store.mongoCollection
import co.there4.hexagon.store.mongoDatabase
import co.there4.hexagon.settings.SettingsManager.settings
import java.lang.System.getenv

import co.there4.hexagon.helpers.err
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.io.Closeable
import java.sql.Connection
import java.sql.ResultSet.CONCUR_READ_ONLY
import java.sql.ResultSet.TYPE_FORWARD_ONLY
import javax.sql.DataSource
import kotlin.reflect.KProperty1

internal const val WORLD_ROWS = 10000

private val DB_HOST = getenv("DBHOST") ?: "localhost"
private val DB = settings["database"] as? String ?: "hello_world"
private val WORLD: String = settings["worldCollection"] as? String ?: "world"
private val FORTUNE: String = settings["fortuneCollection"] as? String ?: "fortune"

private val postgresqlUrl = "jdbc:postgresql://$DB_HOST/$DB?" +
    "jdbcCompliantTruncation=false&" +
    "elideSetAutoCommits=true&" +
    "useLocalSessionState=true&" +
    "cachePrepStmts=true&" +
    "cacheCallableStmts=true&" +
    "alwaysSendSetIsolation=false&" +
    "prepStmtCacheSize=4096&" +
    "cacheServerConfiguration=true&" +
    "prepStmtCacheSqlLimit=2048&" +
    "traceProtocol=false&" +
    "useUnbufferedInput=false&" +
    "useReadAheadInput=false&" +
    "maintainTimeStats=false&" +
    "useServerPrepStmts=true&" +
    "cacheRSMetadata=true"

internal fun createStore(engine: String): Store = when (engine) {
    "mongodb" -> MongoDbStore()
    "postgresql" -> SqlStore(postgresqlUrl)
    else -> error("Unsupported database")
}

internal interface Store {
    fun findFortunes(): List<Fortune>
    fun findWorlds(queries: Int): List<World>
    fun replaceWorlds(queries: Int): List<World>
}

internal class MongoDbStore : Store {
    private val database = mongoDatabase("mongodb://$DB_HOST/$DB")

    internal val worldRepository = repository(WORLD, World::_id)
    internal val fortuneRepository = repository(FORTUNE, Fortune::_id)

    // TODO Find out why it fails when creating index '_id' with background: true
    private inline fun <reified T : Any> repository(name: String, key: KProperty1<T, Int>) =
        MongoIdRepository(T::class, mongoCollection(name, database), key, indexOrder = null)

    override fun findFortunes() = fortuneRepository.findObjects().toList()

    override fun findWorlds(queries: Int) =
        (1..queries).map { worldRepository.find(randomWorld()) }.filterNotNull()

    override fun replaceWorlds(queries: Int) = (1..queries)
        .map { worldRepository.find(randomWorld())?.copy(randomNumber = randomWorld()) ?: err }
        .toList()
        .map {
            worldRepository.replaceObjects(it, bulk = true)
            it
        }
}

internal class SqlStore(jdbcUrl: String) : Store {
    private val SELECT_WORLD = "select * from world where id = ?"
    private val UPDATE_WORLD = "update world set randomNumber = ? where id = ?"
    private val SELECT_FORTUNES = "select * from fortune"

    private val DATA_SOURCE: DataSource

    init {
        val config = HikariConfig()
        config.jdbcUrl = jdbcUrl
        config.maximumPoolSize = 64
        config.username = "benchmarkdbuser"
        config.password = "benchmarkdbpass"
        DATA_SOURCE = HikariDataSource(config)
    }

    override fun findFortunes(): List<Fortune> {
        var fortunes = listOf<Fortune>()

        val connection = KConnection(DATA_SOURCE.connection ?: err)
        connection.use { con: Connection ->
            val rs = con.prepareStatement(SELECT_FORTUNES).executeQuery()
            while (rs.next())
                fortunes += Fortune(rs.getInt(1), rs.getString(2))
        }

        return fortunes
    }

    class KConnection(conn: Connection) : Connection by conn, Closeable

    override fun findWorlds(queries: Int): List<World> {
        var worlds: List<World> = listOf()

        KConnection(DATA_SOURCE.connection).use { con: Connection ->
            val stmtSelect = con.prepareStatement(SELECT_WORLD)

            for (ii in 0..queries - 1) {
                stmtSelect.setInt(1, randomWorld())
                val rs = stmtSelect.executeQuery()
                rs.next()
                val _id = rs.getInt(1)
                worlds += World(_id, _id, rs.getInt(2))
            }
        }

        return worlds
    }

    override fun replaceWorlds(queries: Int): List<World> {
        var worlds: List<World> = listOf()

        KConnection(DATA_SOURCE.connection).use { con: Connection ->
            val stmtSelect = con.prepareStatement(SELECT_WORLD, TYPE_FORWARD_ONLY, CONCUR_READ_ONLY)
            val stmtUpdate = con.prepareStatement(UPDATE_WORLD)

            for (ii in 0..queries - 1) {
                stmtSelect.setInt(1, randomWorld())
                val rs = stmtSelect.executeQuery()
                rs.next()

                val _id = rs.getInt(1)
                val world = World(_id, _id, rs.getInt(2)).copy(randomNumber = randomWorld())
                worlds += world

                stmtUpdate.setInt(1, world.randomNumber)
                stmtUpdate.setInt(2, world.id)
                stmtUpdate.addBatch()

                if (ii % 25 == 0)
                    stmtUpdate.executeBatch()
            }

            stmtUpdate.executeBatch()
        }

        return worlds
    }
}
