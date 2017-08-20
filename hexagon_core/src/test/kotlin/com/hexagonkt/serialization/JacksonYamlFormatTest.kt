package com.hexagonkt.serialization

import org.testng.annotations.Test

@Test class JacksonYamlFormatTest {
    data class Player (val name: String, val number: Int)

    fun yaml_is_serialized_properly() {

    }
}
