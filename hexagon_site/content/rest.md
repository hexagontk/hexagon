title=Hexagon
date=2016-04-13
type=page
status=published
~~~~~~

REST
====

    import com.hexagonkt.store.MongoIdRepository
    import com.hexagonkt.store.mongoDatabase
    import com.hexagonkt.serialization.parse
    import com.hexagonkt.serialization.parseList
    import com.hexagonkt.serialization.serialize
    import org.testng.annotations.Test
    import java.net.URL

    @Test class RestTest {
        data class Parameter (val name: String, val value: String)

        val parametersRepository = MongoIdRepository(
            type = Parameter::class,
            database = mongoDatabase(),
            keyName = "name",
            keyType = String::class,
            keySupplier = { it.name },
            publishEvents = false
        )

        fun crud_handles_entities_nicely () {
            crud(parametersRepository)
            run()

            val client = HttpClient (URL ("http://localhost:${server.bindPort}"))

            val parameter = Parameter("key", "value")
            val modifiedParameter = parameter.copy(value = "changed value")

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

        private fun param (json: String?) = json?.parse (Parameter::class) ?: error
        private fun paramList (json: String?) = json?.parseList (Parameter::class) ?: error
    }
