package co.there4.hexagon.rest

import co.there4.hexagon.repository.MongoIdRepository
import co.there4.hexagon.repository.RepositoryTest
import co.there4.hexagon.serialization.parse
import co.there4.hexagon.serialization.parseList
import co.there4.hexagon.serialization.serialize
import com.github.fakemongo.Fongo
import com.mongodb.MongoClient
import org.testng.annotations.Test
import java.net.URL
import kotlin.reflect.KClass

@Test class RestPackageTest {
    data class Parameter (val name: String, val value: String)

    private val USE_REAL_MONGO_DB = System.getProperty ("useRealMongoDb") != null
    private val collection = createCollection(Parameter::class, "name", String::class) { it.name }

    private fun <T : Any, K : Any> createCollection (
        type: KClass<T>,
        keyName: String,
        keyType: KClass<K>,
        keySupplier: (T) -> K) : MongoIdRepository<T, K> {

        val database = createDatabase (type)
        val collection = database.getCollection(type.simpleName)
        return MongoIdRepository(type, collection, keyName, keyType, keySupplier, true)
    }

    private fun createDatabase (type: KClass<*>) =
        try {
            if (USE_REAL_MONGO_DB) {
                val mongoClient = MongoClient ()
                mongoClient.getDatabase (RepositoryTest::class.simpleName)
            }
            else {
                val fongoClient = Fongo (type.simpleName)
                fongoClient.getDatabase (RepositoryTest::class.simpleName)
            }
        }
        catch (e: Exception) {
            val fongoClient = Fongo (type.simpleName)
            fongoClient.getDatabase (RepositoryTest::class.simpleName)
        }

    fun rest_application_starts_correctly () {
        val repo = MongoIdRepository (
            Parameter::class,
            collection,
            "name",
            String::class,
            { it.name }
        )

        applicationStart {
            handlers {
                crud(repo)
            }
        }

        fun param (json: String?) = json?.parse (Parameter::class) ?: error ("")
        fun paramList (json: String?) = json?.parseList (Parameter::class) ?: error ("")
        val client = HttpClient (URL ("http://localhost:5050"))
        val parameter = Parameter("a", "b")
        val modifiedParameter = parameter.copy(value = "c")

        assert (client.delete("/Parameter/${parameter.name}")?.code() == 200)
        assert (client.getBody("/Parameter") == "[ ]")
        assert (client.post("/Parameter", parameter.serialize())?.code() == 201)
        assert (paramList(client.getBody("/Parameter")) == listOf (parameter))
        assert (param(client.getBody("/Parameter/${parameter.name}")) == parameter)
        assert (client.put("/Parameter", modifiedParameter.serialize())?.code() == 200)
        assert (param(client.getBody("/Parameter/${modifiedParameter.name}")) == modifiedParameter)
        assert (client.delete("/Parameter/${parameter.name}")?.code() == 200)
        assert (client.get("/Parameter/${parameter.name}")?.code() == 404)
        assert (client.getBody("/Parameter") == "[ ]")
    }
}
