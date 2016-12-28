package co.there4.hexagon

import co.there4.hexagon.repository.MongoIdRepository
import co.there4.hexagon.repository.mongoCollection
import java.lang.System.getenv

import co.there4.hexagon.settings.SettingsManager.setting
import co.there4.hexagon.repository.mongoDatabase
import co.there4.hexagon.settings.SettingsManager.requireSetting
import co.there4.hexagon.util.err
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.io.Closeable
import kotlin.reflect.KProperty1
import java.sql.BatchUpdateException
import java.sql.Connection
import java.sql.PreparedStatement
import java.util.concurrent.ThreadLocalRandom
import javax.sql.DataSource


internal val FORTUNE_MESSAGES = setOf(
    "fortune: No such file or directory",
    "A computer scientist is someone who fixes things that aren't broken.",
    "After enough decimal places, nobody gives a damn.",
    "A bad random number generator: 1, 1, 1, 1, 1, 4.33e+67, 1, 1, 1",
    "A computer program does what you tell it to do, not what you want it to do.",
    "Emacs is a nice operating system, but I prefer UNIX. — Tom Christaensen",
    "Any program that runs right is obsolete.",
    "A list is only as strong as its weakest link. — Donald Knuth",
    "Feature: A bug with seniority.",
    "Computers make very fast, very accurate mistakes.",
    "<script>alert(\"This should not be displayed in a browser alert box.\");</script>",
    "フレームワークのベンチマーク"
)

internal val DB_ROWS = 10000

private val DB_HOST = getenv("DBHOST") ?: "localhost"
private val DB = setting<String>("database") ?: "hello_world"
private val WORLD: String = setting<String>("worldCollection") ?: "world"
private val FORTUNE: String = setting<String>("fortuneCollection") ?: "fortune"

private val database = mongoDatabase("mongodb://$DB_HOST/$DB")

internal val worldRepository = repository(WORLD, World::_id)
internal val fortuneRepository = repository(FORTUNE, Fortune::_id)

// TODO Find out why it fails when creating index '_id' with background: true
private inline fun <reified T : Any> repository(name: String, key: KProperty1<T, Int>) =
    MongoIdRepository(T::class, mongoCollection(name, database), key, indexOrder = null)

internal fun initialize() {
    fortuneRepository.drop()
    if (fortuneRepository.isEmpty()) {
        val fortunes = FORTUNE_MESSAGES.mapIndexed { ii, fortune -> Fortune(ii + 1, fortune) }
        fortuneRepository.insertManyObjects(fortunes)
    }

    worldRepository.drop()
    if (worldRepository.isEmpty()) {
        val world = (1..DB_ROWS).map { World(it, it) }
        worldRepository.insertManyObjects(world)
    }
}

internal fun findFortunes() = fortuneRepository.findObjects().toList()

internal fun findWorld() = worldRepository.find(rnd())

internal fun replaceWorld(newWorld: World) {
    worldRepository.replaceObject(newWorld)
}

internal interface Repository {
    fun findFortunes(): List<Fortune>
    fun accessWorlds(queries: Int, update: Boolean): List<World>

    fun findWorlds(queries: Int): List<World> = accessWorlds(queries, false)
    fun updateWorlds(queries: Int): List<World> = accessWorlds(queries, true)
}

internal class MySqlRepository : Repository {
    private val AUTOCOMMIT = System.getProperty("sabina.benchmark.autocommit") != null
    private val SELECT_WORLD = "select * from world where id = ?"
    private val UPDATE_WORLD = "update world set randomNumber = ? where id = ?"
    private val SELECT_FORTUNES = "select * from fortune"

    private val DATA_SOURCE: DataSource

    init {
        val config = HikariConfig()
        config.jdbcUrl = requireSetting<String>("mysql.uri")
        config.maximumPoolSize = 256
        DATA_SOURCE = HikariDataSource(config)
    }

    private fun commitUpdate(con: Connection, stmtUpdate: PreparedStatement) {
        var count = 0
        var retrying: Boolean

        do {
            try {
                stmtUpdate.executeBatch()
                retrying = false
            }
            catch (e: BatchUpdateException) {
                retrying = true
            }
        }
        while (count++ < 10 && retrying)

        con.commit()
    }

    private fun updateWorld(world: World, stmtUpdate: PreparedStatement) {
        stmtUpdate.setInt(1, world.randomNumber)
        stmtUpdate.setInt(2, world.id)

        if (AUTOCOMMIT)
            stmtUpdate.executeUpdate()
        else
            stmtUpdate.addBatch()
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

    override fun accessWorlds(queries: Int, update: Boolean): List<World> {
        val worlds: MutableList<World> = mutableListOf()

        KConnection(DATA_SOURCE.connection).use { con: Connection ->
            if (update)
                con.autoCommit = AUTOCOMMIT

            val random = ThreadLocalRandom.current()
            val stmtSelect = con.prepareStatement(SELECT_WORLD)
            val stmtUpdate = if (update) con.prepareStatement(UPDATE_WORLD) else null

            for (ii in 0..queries - 1) {
                stmtSelect.setInt(1, random.nextInt(DB_ROWS) + 1)
                val rs = stmtSelect.executeQuery()
                while (rs.next()) {
                    worlds[ii] = World(rs.getInt(1), rs.getInt(2))

                    if (stmtUpdate != null) {
                        worlds[ii] = World(worlds[ii].id, random.nextInt(DB_ROWS) + 1)
                        updateWorld(worlds[ii], stmtUpdate)
                    }
                }
            }

            if (stmtUpdate != null && !AUTOCOMMIT)
                commitUpdate(con, stmtUpdate)
        }

        return worlds
    }
}
