title=Hexagon
date=2016-04-13
type=page
status=published
~~~~~~

REST
====

    package co.there4.hexagon.rest

    import co.there4.hexagon.repository.MongoIdRepository
    import co.there4.hexagon.repository.mongoDatabase
    import co.there4.hexagon.serialization.parse
    import co.there4.hexagon.serialization.parseList
    import co.there4.hexagon.serialization.serialize
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
            val server = applicationStart {
                serverConfig {
                    port(0)
                }
                handlers {
                    crud(parametersRepository)
                }
            }

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

        private fun param (json: String?) = json?.parse (Parameter::class) ?: error ("")
        private fun paramList (json: String?) = json?.parseList (Parameter::class) ?: error ("")
    }
