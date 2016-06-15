package co.there4.hexagon.rest

import co.there4.hexagon.repository.MongoIdRepository
import co.there4.hexagon.repository.mongoDatabase
import co.there4.hexagon.serialization.parse
import co.there4.hexagon.serialization.parseList
import co.there4.hexagon.serialization.serialize
import co.there4.hexagon.web.Client
import co.there4.hexagon.web.jetty.JettyServer
import co.there4.hexagon.web.server
import co.there4.hexagon.web.stop
import co.there4.hexagon.web.run
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

        val server = JettyServer (bindPort = 5020)

        server.crud(repo)
        server.run()

        fun param (json: String?) = json?.parse (Country::class) ?: error ("")
        fun paramList (json: String?) = json?.parseList (Country::class) ?: error ("")
        val url = "http://${server.bindAddress.hostAddress}:${server.bindPort}"
        val client = Client (URL (url), false)
        val parameter = Country(34, "es")
        val changedParameter = parameter.copy(code = "fr")

        assert (client.delete("/Country/${parameter.id}").statusCode == 200)
        assert (client.get("/Country").responseBody == "[ ]")
        assert (client.post("/Country", parameter.serialize()).statusCode == 201)
        assert (client.post("/Country", parameter.serialize()).statusCode == 500)
        assert (paramList(client.get("/Country").responseBody) == listOf (parameter))
        assert (param(client.get("/Country/${parameter.id}").responseBody) == parameter)
        assert (client.put("/Country", changedParameter.serialize()).statusCode == 200)
        val changedParameterId = changedParameter.id
        assert (param(client.get("/Country/$changedParameterId").responseBody) == changedParameter)
        assert (client.delete("/Country/${parameter.id}").statusCode == 200)
        assert (client.get("/Country/${parameter.id}").statusCode == 404)
        assert (client.get("/Country").responseBody == "[ ]")

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

        stop()
        server = JettyServer()
        crud(repo)
        run()

        fun param (json: String?) = json?.parse (Parameter::class) ?: error ("")
        fun paramList (json: String?) = json?.parseList (Parameter::class) ?: error ("")
        val client = Client (URL ("http://localhost:${server.bindPort}"), false)
        val parameter = Parameter("a", "b")
        val modifiedParameter = parameter.copy(value = "c")

        assert (client.delete("/Parameter/${parameter.name}").statusCode == 200)
        assert (client.get("/Parameter").responseBody == "[ ]")
        assert (client.post("/Parameter", parameter.serialize()).statusCode == 201)
        assert (paramList(client.get("/Parameter").responseBody) == listOf (parameter))
        assert (param(client.get("/Parameter/${parameter.name}").responseBody) == parameter)
        assert (client.put("/Parameter", modifiedParameter.serialize()).statusCode == 200)
        assert (param(client.get("/Parameter/${modifiedParameter.name}").responseBody) == modifiedParameter)
        assert (client.delete("/Parameter/${parameter.name}").statusCode == 200)
        assert (client.get("/Parameter/${parameter.name}").statusCode == 404)
        assert (client.get("/Parameter").responseBody == "[ ]")

        stop()
    }
}
