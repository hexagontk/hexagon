package co.there4.hexagon.rest

import co.there4.hexagon.repository.MongoIdRepository
import co.there4.hexagon.repository.RepositoryTest
import com.github.fakemongo.Fongo
import com.mongodb.MongoClient
import org.testng.annotations.Test
import java.net.URL
import kotlin.reflect.KClass

@Test class RestPackageTest {
    private data class Parameter (val id: String, val value: String)

    val USE_REAL_MONGO_DB = System.getProperty ("useRealMongoDb") != null
    private val collection = createCollection(Parameter::class, "id", String::class) { it.id }

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
            "id",
            String::class,
            { it.id }
        )

        appStart {
            handlers {
                crud(repo)
            }
        }

        val client = HttpClient (URL ("http://localhost:5050"))

        // NOTE Fails because to parse the key as JSON, it should be ("a"), not (a)
        client.delete("/Parameter/a")

        assert (client.post("/Parameter", """{"id":"a","value":"b"}""")?.code() == 201)
//        assert (client.getBody("/Parameter/a") == """{"id":"a","value":"b"}""")
//        assert (client.put("/Parameter", """{"id":"a","value":"c"}""")?.code() == 200)
//        assert (client.getBody("/Parameter/a") == """{"id":"a","value":"c"}""")
//        assert (client.delete("/Parameter/a")?.code() == 200)
//        assert (client.get("/Parameter/a")?.code() == 404)
    }
}
