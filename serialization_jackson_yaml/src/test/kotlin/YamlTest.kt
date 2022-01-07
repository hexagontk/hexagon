package com.hexagonkt.serialization.jackson.yaml

import com.hexagonkt.core.converters.ConvertersManager
import com.hexagonkt.core.converters.convert
import com.hexagonkt.core.helpers.require
import com.hexagonkt.core.helpers.requireKeys
import com.hexagonkt.serialization.*
import com.hexagonkt.serialization.test.SerializationTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.net.URL
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class YamlTest : SerializationTest() {

    override val format: SerializationFormat = Yaml
    override val urls: List<URL> = listOf(
        URL("classpath:data/companies.yml"),
        URL("classpath:data/company.yml"),
    )

    data class Player (val name: String, val number: Int, val category: ClosedRange<Int>)

    @BeforeAll fun setUpSerializationManager() {
        SerializationManager.formats = linkedSetOf(Yaml)

        ConvertersManager.register(Map::class to Player::class) {
            Player(
                name = it.requireKeys(Player::name.name),
                number = it.requireKeys(Player::number.name),
                category = it.requireKeys<Map<String, Int>>(Player::category.name).let { map ->
                    val start = map.require(ClosedRange<*>::start.name)
                    val endInclusive = map.require(ClosedRange<*>::endInclusive.name)
                    start..endInclusive
                }
            )
        }
    }

    @Test fun `YAML is serialized properly` () {
        val player = Player("Michael", 23, 18..65)
        val serializedPlayer = player.serialize(Yaml)
        val deserializedPlayer = serializedPlayer.parse(Yaml).convert<Player>()

        assertEquals(deserializedPlayer.name, player.name)
        assertEquals(deserializedPlayer.number, player.number)
        assertEquals(deserializedPlayer.category.start, player.category.start)
        assertEquals(deserializedPlayer.category.endInclusive, player.category.endInclusive)
    }

    @Suppress("UNCHECKED_CAST") // Required by test
    @Test fun `Parse valid YAML` () {
        val parse = """
            - a: b
            - b: c
            - c: d
        """.parse(Yaml) as List<Map<Any, *>>
        assertEquals("b", parse.first()["a"])
    }
}
