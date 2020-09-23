package com.hexagonkt.serialization

import com.hexagonkt.helpers.toStream
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertFailsWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class YamlTest {

    enum class DeviceOs { ANDROID, IOS }

    data class Device(
        val id: String,
        val brand: String,
        val model: String,
        val os: DeviceOs,
        val osVersion: String,

        val alias: String = "$brand $model"
    )

    data class Player (val name: String, val number: Int, val category: ClosedRange<Int>)

    @BeforeAll fun setUpSerializationManager() {
        SerializationManager.formats = linkedSetOf(Json, Yaml)
    }

    @Test fun `YAML is serialized properly` () {
        val player = Player("Michael", 23, 18..65)
        val serializedPlayer = player.serialize(Yaml)
        val deserializedPlayer = serializedPlayer.parse(Player::class, Yaml)

        assert (player.name == deserializedPlayer.name)
        assert (player.number == deserializedPlayer.number)
        assert (player.category.start == deserializedPlayer.category.start)
        assert (player.category.endInclusive == deserializedPlayer.category.endInclusive)
    }

    @Test fun `Parse invalid YAML range` () {
        assertFailsWith<ParseException> {
            """
            name: Michael
            number: 23
            category: error
            """
            .trimIndent()
            .parse(Player::class, Yaml)
        }
    }

    @Test fun `Parse invalid YAML range start` () {
        assertFailsWith<ParseException> {
            """
            name: Michael
            number: 23
            category:
                error: 18
                endInclusive: 65
            """
            .trimIndent()
            .parse(Player::class, Yaml)
        }
    }

    @Test fun `Parse invalid YAML range end` () {
        assertFailsWith<ParseException> {
            """
            name: Michael
            number: 23
            category:
                start: 18
                error: 65
            """
            .trimIndent()
            .parse(Player::class, Yaml)
        }
    }

    @Test fun `Parse valid YAML` () {
        val parse = """
            - a: b
            - b: c
            - c: d
        """.trimIndent().toStream().parseObjects<Map<String, *>>(Yaml)
        assert(parse[0]["a"] == "b")
    }

    @Test fun `Serialize by content type` () {
        val result = mapOf("aKey" to 1, "bKey" to 2).serialize(Yaml.contentType)
        assert(result.contains("aKey") && result.contains("bKey"))
    }

    @Test fun `Parse exceptions contains failed field`() {
        try {
            """
            {
              "id" : "f",
              "brand" : "br",
              "model" : "mo",
              "os" : "ANDROI",
              "osVersion" : "v",
              "alias" : "al"
            }
            """.parse(Device::class)

            assert(false) { "Exception expected" }
        }
        catch (e: ParseException) {
            assert(e.field == "com.hexagonkt.serialization.YamlTest\$Device[\"os\"]")
        }
    }

    @Test fun `Invalid format exceptions field is 'null'`() {
        try {
            """
              "id" "f",
              "brand" : "br",
              "model" : "mo",
              "os" : "ANDROI",
              "osVersion" : "v",
              "alias" : "al"
            }
            """.parse(Device::class)

            assert(false) { "Exception expected" }
        }
        catch (e: ParseException) {
            assert(e.field == "")
        }
    }

    @Test fun `Parse an invalid class throws exception`() {
        try {
            """
            [
                {
                  "id" : "f",
                  "brand" : "br",
                  "model" : "mo",
                  "os" : "ANDROI",
                  "osVersion" : "v",
                  "alias" : "al"
                }
            ]
            """.parseObjects<Device>()

            assert(false) { "Exception expected" }
        }
        catch (e: ParseException) {
            val fieldFullName = "com.hexagonkt.serialization.YamlTest\$Device[\"os\"]"
            assert(e.field == "java.util.ArrayList[0]->$fieldFullName")
        }
    }
}
