package com.hexagonkt.serialization

import com.hexagonkt.helpers.toStream
import org.testng.annotations.Test

@Test class JacksonYamlFormatTest {
    data class Player (val name: String, val number: Int, val category: ClosedRange<Int>)

    fun `yaml is serialized properly` () {
        val player = Player("Michael", 23, 18..65)
        val serializedPlayer = player.serialize(YamlFormat)
        val deserializedPlayer = serializedPlayer.parse(Player::class, YamlFormat)

        assert (player.name == deserializedPlayer.name)
        assert (player.number == deserializedPlayer.number)
        assert (player.category.start == deserializedPlayer.category.start)
        assert (player.category.endInclusive == deserializedPlayer.category.endInclusive)
    }

    @Test(expectedExceptions = arrayOf(IllegalStateException::class))
    fun `parse invalid YAML range` () {
        """
            name: Michael
            number: 23
            category: error
        """
        .trimIndent()
        .parse(Player::class, YamlFormat)
    }

    @Test(expectedExceptions = arrayOf(IllegalStateException::class))
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

    @Test(expectedExceptions = arrayOf(IllegalStateException::class))
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

    fun `parse valid YAML` () {
        val parse = """
            - a: b
            - b: c
            - c: d
        """.trimIndent().toStream().parseList(YamlFormat)
        assert(parse[0]["a"] == "b")
    }
}
