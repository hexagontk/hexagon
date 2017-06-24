package co.there4.hexagon.rest

import co.there4.hexagon.client.Client
import co.there4.hexagon.store.MongoIdRepository
import co.there4.hexagon.store.mongoDatabase
import co.there4.hexagon.store.mongoRepository
import co.there4.hexagon.serialization.parse
import co.there4.hexagon.serialization.parseList
import co.there4.hexagon.serialization.serialize
import co.there4.hexagon.helpers.error
import co.there4.hexagon.server.*
import co.there4.hexagon.server.jetty.JettyServletEngine
import org.testng.annotations.Test
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

@Test class RestPackageTest {
    data class Parameter (val name: String, val value: String)
    data class Country (val id: Int, val code: String)
    data class Address (val street: String, val number: Int, val postcode: String)

    private val parameters = createCollection(Parameter::class, Parameter::name)
    private val countries = createCollection(Country::class, Country::id)

    private fun <T : Any, K : Any> createCollection (type: KClass<T>, key: KProperty1<T, K>) =
        MongoIdRepository(type, mongoDatabase(), key)

    fun int_keyed_repositories_are_handled_properly () {
        val repo = MongoIdRepository (Country::class, countries, Country::id)

        val server = Server(JettyServletEngine(), bindPort = 0)

        server.crud(repo)
        server.run()

        fun param (json: String?) = json?.parse (Country::class) ?: error
        fun paramList (json: String?) = json?.parseList (Country::class) ?: error
        val url = "http://${server.bindAddress.hostAddress}:${server.runtimePort}"
        val client = Client (url, useCookies = false)
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

        val countryList = listOf(parameter, Country(1, "us"))
        assert(client.post("/Country/list", countryList.serialize()).statusCode == 201)
        assert(client.get("/Country/ids").responseBody.parseList(Int::class) == listOf(34, 1))
        assert(client.delete("/Country/34,1").statusCode == 200)
        assert (client.get("/Country").responseBody == "[ ]")

        server.stop()
    }

    fun rest_application_starts_correctly () {
        val repo = MongoIdRepository (Parameter::class, parameters, Parameter::name)

        val server = Server(JettyServletEngine(), bindPort = 0)
        server.crud(repo)
        server.run()

        fun param (json: String?) = json?.parse (Parameter::class) ?: error
        fun paramList (json: String?) = json?.parseList (Parameter::class) ?: error
        val client = Client("http://localhost:${server.runtimePort}", useCookies = false)
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

        server.stop()
    }

    fun simple_crud_starts_correctly () {
        val addresses = mongoRepository<Address>()

        val server = Server(JettyServletEngine(), bindPort = 0)
        server.crud(addresses)
        server.run()

        fun param (json: String?) = json?.parse (Address::class) ?: error
        fun paramList (json: String?) = json?.parseList (Address::class) ?: error

        val client = Client("http://localhost:${server.runtimePort}", useCookies = false)

        val parameter = Address("a", 0, "b")
        val modifiedParameter = parameter.copy(postcode = "c")

        assert (client.delete("/Address?postcode=${parameter.postcode}").statusCode == 200)
        assert (client.get("/Address").responseBody == "[ ]")
        assert (client.post("/Address", parameter.serialize()).statusCode == 201)
        assert (paramList(client.get("/Address").responseBody) == listOf (parameter))
        assert (paramList(client.get("/Address?postcode=${parameter.postcode}").responseBody) == listOf(parameter))
        assert (client.delete("/Address?postcode=${parameter.postcode}").statusCode == 200)
        assert (client.get("/Address").responseBody == "[ ]")

        val addressList = listOf(parameter, modifiedParameter)
        assert (client.post("/Address/list", addressList.serialize()).statusCode == 201)
        assert (client.get("/Address/count").responseBody == "2")

        assert (client.delete("/Address").statusCode == 400)

        assert (paramList(client.get("/Address?limit=1&skip=1").responseBody) == listOf(modifiedParameter))

        assert (client.delete("/Address?postcode=b,c").statusCode == 200)
        assert (client.get("/Address/count").responseBody == "0")

        server.stop()
    }
}
