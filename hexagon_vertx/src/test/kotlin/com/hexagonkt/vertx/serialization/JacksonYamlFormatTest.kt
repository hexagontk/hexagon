package com.hexagonkt.vertx.serialization

import com.hexagonkt.toStream
import com.hexagonkt.vertx.Device
import org.junit.Test
import kotlin.test.fail

class JacksonYamlFormatTest {
    data class Player (val name: String, val number: Int, val category: ClosedRange<Int>)

    @Test fun `yaml is serialized properly` () {
        val player = Player("Michael", 23, 18..65)
        val serializedPlayer = player.serialize(YamlFormat)
        val deserializedPlayer = serializedPlayer.parse(Player::class, YamlFormat)

        assert (player.name == deserializedPlayer.name)
        assert (player.number == deserializedPlayer.number)
        assert (player.category.start == deserializedPlayer.category.start)
        assert (player.category.endInclusive == deserializedPlayer.category.endInclusive)
    }

    @Test(expected = ParseException::class)
    fun `parse invalid YAML range` () {
        """
            name: Michael
            number: 23
            category: error
        """
        .trimIndent()
        .parse(Player::class, YamlFormat)
    }

    @Test(expected = ParseException::class)
    fun `parse invalid YAML range start` () {
        """
            name: Michael
            number: 23
            category:
                error: 18
                endInclusive: 65
        """
        .trimIndent()
        .parse(Player::class, YamlFormat)
    }

    @Test(expected = ParseException::class)
    fun `parse invalid YAML range end` () {
        """
            name: Michael
            number: 23
            category:
                start: 18
                error: 65
        """
        .trimIndent()
        .parse(Player::class, YamlFormat)
    }

    @Test fun `parse valid YAML` () {
        val parse = """
            - a: b
            - b: c
            - c: d
        """.trimIndent().toStream().parseList(YamlFormat)
        assert(parse[0]["a"] == "b")
    }

    @Test fun `Serialize by content type` () {
        val result = mapOf("aKey" to 1, "bKey" to 2).serialize(YamlFormat.contentType)
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

            fail("Exception expected")
        }
        catch (e: ParseException) {
            assert(e.field == "com.hexagonkt.vertx.Device[\"os\"]")
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

            fail("Exception expected")
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
            """.parseList(Device::class)

            fail("Exception expected")
        }
        catch (e: ParseException) {
            assert(e.field == "java.util.ArrayList[0]->com.hexagonkt.vertx.Device[\"os\"]")
        }
    }
}
