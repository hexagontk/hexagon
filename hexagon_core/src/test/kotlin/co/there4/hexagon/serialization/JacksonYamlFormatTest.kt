package co.there4.hexagon.serialization

import org.testng.annotations.Test

@Test class JacksonYamlFormatTest {
    data class Player (val name: String, val number: Int)

    fun yaml_is_serialized_properly() {

    }
}
