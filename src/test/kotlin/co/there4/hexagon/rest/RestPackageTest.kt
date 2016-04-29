package co.there4.hexagon.rest

import co.there4.hexagon.repository.MongoIdRepository
import co.there4.hexagon.repository.mongoDatabase
import co.there4.hexagon.serialization.parse
import co.there4.hexagon.serialization.parseList
import co.there4.hexagon.serialization.serialize
import org.testng.annotations.Test
import java.net.URL
import kotlin.reflect.KClass

@Test class RestPackageTest {
    data class Parameter (val name: String, val value: String)
    data class Country (val id: Int, val code: String)

    private val parameters = createCollection(Parameter::class, "name", String::class) { it.name }
    private val countries = createCollection(Country::class, "id", Int::class) { it.id }

    private fun <T : Any, K : Any> createCollection (
        type: KClass<T>,
        keyName: String,
        keyType: KClass<K>,
        keySupplier: (T) -> K) : MongoIdRepository<T, K> =
            MongoIdRepository(type, mongoDatabase(), keyName, keyType, keySupplier, true)

    fun int_keyed_repositories_are_handled_properly () {
        val repo = MongoIdRepository (
            Country::class,
            countries,
            "id",
            Int::class,
            { it.id }
        )

        val server = applicationStart {
            serverConfig {
                port(0)
            }
            handlers {
                crud(repo)
            }
        }

        fun param (json: String?) = json?.parse (Country::class) ?: error ("")
        fun paramList (json: String?) = json?.parseList (Country::class) ?: error ("")
        val client = HttpClient (URL ("http://localhost:${server.bindPort}"))
        val parameter = Country(34, "es")
        val modifiedParameter = parameter.copy(code = "fr")

        assert (client.delete("/Country/${parameter.id}").code() == 200)
        assert (client.getBody("/Country") == "[ ]")
        assert (client.post("/Country", parameter.serialize()).code() == 201)
        assert (client.post("/Country", parameter.serialize()).code() == 500)
        assert (paramList(client.getBody("/Country")) == listOf (parameter))
        assert (param(client.getBody("/Country/${parameter.id}")) == parameter)
        assert (client.put("/Country", modifiedParameter.serialize()).code() == 200)
        assert (param(client.getBody("/Country/${modifiedParameter.id}")) == modifiedParameter)
        assert (client.delete("/Country/${parameter.id}").code() == 200)
        assert (client.get("/Country/${parameter.id}").code() == 404)
        assert (client.getBody("/Country") == "[ ]")

        server.stop()
    }

    fun rest_application_starts_correctly () {
        val repo = MongoIdRepository (
            Parameter::class,
            parameters,
            "name",
            String::class,
            { it.name }
        )

        val server = applicationStart {
            serverConfig {
                port(0)
            }
            handlers {
                crud(repo)
            }
        }

        fun param (json: String?) = json?.parse (Parameter::class) ?: error ("")
        fun paramList (json: String?) = json?.parseList (Parameter::class) ?: error ("")
        val client = HttpClient (URL ("http://localhost:${server.bindPort}"))
        val parameter = Parameter("a", "b")
        val modifiedParameter = parameter.copy(value = "c")

        assert (client.delete("/Parameter/${parameter.name}").code() == 200)
        assert (client.getBody("/Parameter") == "[ ]")
        assert (client.post("/Parameter", parameter.serialize()).code() == 201)
        assert (paramList(client.getBody("/Parameter")) == listOf (parameter))
        assert (param(client.getBody("/Parameter/${parameter.name}")) == parameter)
        assert (client.put("/Parameter", modifiedParameter.serialize()).code() == 200)
        assert (param(client.getBody("/Parameter/${modifiedParameter.name}")) == modifiedParameter)
        assert (client.delete("/Parameter/${parameter.name}").code() == 200)
        assert (client.get("/Parameter/${parameter.name}").code() == 404)
        assert (client.getBody("/Parameter") == "[ ]")

        server.stop()
    }
}
