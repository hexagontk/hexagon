package com.hexagonkt.serialization

import org.testng.annotations.Test

@Test class CsvFormatTest {
    data class Player (val name: String, val number: Int, val category: Int)

    @Test fun `CSV is serialized properly` () {
        val player = Player("Michael", 23, 18)
        val serializedPlayer = player.serialize(CsvFormat)
        val deserializedPlayer = serializedPlayer.parse(Player::class, CsvFormat)

        assert (player.name == deserializedPlayer.name)
        assert (player.number == deserializedPlayer.number)
        assert (player.category == deserializedPlayer.category)
    }

    @Test(expectedExceptions = [ ParseException::class ])
    fun `Parse invalid CSV type` () {
        "Michael,23,error".parse(Player::class, CsvFormat)
    }

    // TODO
    @Test(enabled = false) fun `Serialize by content type` () {
        val result = mapOf("aKey" to 1, "bKey" to 2).serialize(CsvFormat.contentType)
        assert(result.contains("aKey") && result.contains("bKey"))
    }

    @Test fun `Parse exceptions contains failed field`() {
        try {
            "f,br,mo,ANDROI,v,al".parse(Device::class, CsvFormat)

            assert(false) { "Exception expected" }
        }
        catch (e: ParseException) {
            assert(e.field == "com.hexagonkt.serialization.Device[\"os\"]")
        }
    }

    @Test fun `Invalid format exceptions field is 'null'`() {
        try {
            "f,br,mo,ANDROI,v,\"al".parse(Device::class)

            assert(false) { "Exception expected" }
        }
        catch (e: ParseException) {
            assert(e.field == "")
        }
    }

    // TODO
    @Test(enabled = false) fun `Parse an invalid class throws exception`() {
        try {
            "f,br,mo,ANDROI,v,al".parseList(Device::class)

            assert(false) { "Exception expected" }
        }
        catch (e: ParseException) {
            assert(e.field == "java.util.ArrayList[0]->com.hexagonkt.serialization.Device[\"os\"]")
        }
    }
}
