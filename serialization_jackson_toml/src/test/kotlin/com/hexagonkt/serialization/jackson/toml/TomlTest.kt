package com.hexagonkt.serialization.jackson.toml

import com.hexagonkt.core.require
import com.hexagonkt.core.requireKeys
import com.hexagonkt.serialization.*
import com.hexagonkt.serialization.test.SerializationTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.net.URL
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class TomlTest : SerializationTest() {

    override val format: SerializationFormat = Toml
    override val urls: List<URL> = listOf(
        URL("classpath:data/companies.toml"),
        URL("classpath:data/company.toml"),
    )

    data class Player (val name: String, val number: Int, val category: ClosedRange<Int>)

    @BeforeAll fun setUpSerializationManager() {
        SerializationManager.formats = linkedSetOf(Toml)
    }

    private fun Map<*, *>.convert(): Player =
        Player(
            name = requireKeys(Player::name.name),
            number = requireKeys(Player::number.name),
            category = requireKeys<Map<String, Int>>(Player::category.name).let { map ->
                val start = map.require(ClosedRange<*>::start.name)
                val endInclusive = map.require(ClosedRange<*>::endInclusive.name)
                start..endInclusive
            }
        )

    @Test fun `TOML is serialized properly` () {
        val player = Player("Michael", 23, 18..65)
        val serializedPlayer = player.serialize(Toml)
        val deserializedPlayer = serializedPlayer.parseMap(Toml).convert()

        assertEquals(deserializedPlayer.name, player.name)
        assertEquals(deserializedPlayer.number, player.number)
        assertEquals(deserializedPlayer.category.start, player.category.start)
        assertEquals(deserializedPlayer.category.endInclusive, player.category.endInclusive)
    }

    @Suppress("UNCHECKED_CAST") // Required by test
    @Test fun `Parse valid TOML` () {
        val map = """
            [[_]]
            a = "b"
            [[_]]
            b = "c"
            [[_]]
            c = "d"
        """.parseMap(Toml) as Map<String, *>
        val parse = map.require("_") as List<Map<Any, *>>
        assertEquals("b", parse.first()["a"])
    }

    @Test fun `Pretty print TOML`() {
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
            "key = [{a = 'str', b = [1, 2]}, 1, 2]",
            Toml.serialize(map).trim()
        )

        val testMap = mapOf("key" to "123")

        assertEquals("key = '123'", Toml.serialize(testMap).trim())
        assertEquals("key = 123", Toml.serialize(mapOf("key" to 123)).trim())
    }
}
