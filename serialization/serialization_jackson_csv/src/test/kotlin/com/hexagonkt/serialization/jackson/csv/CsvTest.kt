package com.hexagonkt.serialization.jackson.csv

import com.hexagonkt.serialization.*
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import kotlin.test.assertEquals

@TestInstance(PER_CLASS)
internal class CsvTest {

    @BeforeAll fun initialize() {
        SerializationManager.formats = linkedSetOf(Csv)
    }

    @Test fun `CSV is serialized properly`() {
        val player = listOf("Michael", "23", "18")
        val serializedPlayer = player.serialize(Csv)
        val deserializedPlayer = serializedPlayer.parse(Csv)

        assertEquals(listOf(player), deserializedPlayer)

        val players = listOf(player, listOf("Magic", "32", "36"))
        val serializedPlayers = players.serialize(Csv)
        val deserializedPlayers = serializedPlayers.parse(Csv)

        assertEquals(players, deserializedPlayers)
    }

    @Test fun `Serialize an empty list works as expected`() {
        assertEquals("", emptyList<Any>().serialize(Csv))
    }
}
