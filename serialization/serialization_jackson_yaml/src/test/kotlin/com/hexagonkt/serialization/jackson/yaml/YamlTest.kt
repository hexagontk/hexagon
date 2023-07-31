package com.hexagonkt.serialization.jackson.yaml

import com.hexagonkt.core.fieldsMapOfNotNull
import com.hexagonkt.core.require
import com.hexagonkt.core.requirePath
import com.hexagonkt.core.urlOf
import com.hexagonkt.serialization.*
import com.hexagonkt.serialization.test.SerializationTest
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test
import org.junit.jupiter.api.TestInstance
import java.net.URL
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class YamlTest : SerializationTest() {

    override val format: SerializationFormat = Yaml
    override val urls: List<URL> = listOf(
        urlOf("classpath:data/companies.yml"),
        urlOf("classpath:data/company.yml"),
    )

    data class Player(val name: String, val number: Int, val category: ClosedRange<Int>)

    @BeforeAll fun setUpSerializationManager() {
        SerializationManager.formats = linkedSetOf(Yaml)
    }

    private fun Map<*, *>.convert(): Player =
        Player(
            name = requirePath(Player::name.name),
            number = requirePath(Player::number.name),
            category = requirePath<Map<String, Int>>(Player::category.name).let { map ->
                val start = map.require(ClosedRange<*>::start.name)
                val endInclusive = map.require(ClosedRange<*>::endInclusive.name)
                start..endInclusive
            }
        )

    private fun Player.convert(): Map<*, *> =
        fieldsMapOfNotNull(
            Player::name to name,
            Player::number to number,
            Player::category to fieldsMapOfNotNull(
                ClosedRange<*>::start to category.start,
                ClosedRange<*>::endInclusive to category.endInclusive,
            )
        )

    @Test fun `YAML is serialized properly` () {
        val player = Player("Michael", 23, 18..65)
        val serializedPlayer = player.convert().serialize(Yaml)
        val deserializedPlayer = serializedPlayer.parseMap(Yaml).convert()

        assertEquals(deserializedPlayer.name, player.name)
        assertEquals(deserializedPlayer.number, player.number)
        assertEquals(deserializedPlayer.category.start, player.category.start)
        assertEquals(deserializedPlayer.category.endInclusive, player.category.endInclusive)
    }

    @Test fun `Parse valid YAML` () {
        val parse = """
            - a: b
            - b: 0.1
            - c: true
        """.parseList(Yaml).map { it as Map<*, *> }
        assertEquals("b", parse.component1()["a"])
        assertEquals(0.1, parse.component2()["b"])
        assertEquals(true, parse.component3()["c"])
    }

    @Test fun `Pretty print YAML`() {
        val map = mapOf(
            "key" to listOf(
                mapOf(
                    "a" to "str",
                    "b" to listOf(1, 2),
                ),
                1,
                2,
            )
        )

        assertEquals(
            """
            key:
              - a: str
                b:
                  - 1
                  - 2
              - 1
              - 2
            """.trimIndent(),
            Yaml.serialize(map).trim()
        )

        assertEquals(
            """
            key:
            - a: "str"
              b:
              - 1
              - 2
            - 1
            - 2
            """.trimIndent(),
            Yaml.raw.serialize(map).trim()
        )

        val testMap = mapOf("key" to "123")

        assertEquals("""key: "123"""", Yaml.serialize(testMap).trim())
        assertEquals("""key: "123"""", Yaml.raw.serialize(testMap).trim())

        assertEquals("key: 123", Yaml.serialize(mapOf("key" to 123)).trim())
        assertEquals("key: 123", Yaml.raw.serialize(mapOf("key" to 123)).trim())
    }
}
