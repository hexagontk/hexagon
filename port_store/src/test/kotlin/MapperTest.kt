package com.hexagonkt.store

import org.testng.annotations.Test

@Test class MapperTest {

    @Test fun `Default mapper methods return its own parameters`() {
        val testMapper = object : Mapper<String> {
            override fun toStore(instance: String): Map<String, Any> = emptyMap()
            override fun fromStore(map: Map<String, Any>): String = ""
        }

        assert(testMapper.fromStore("property", "value") == "value")
        assert(testMapper.toStore("property", "value") == "value")
    }
}
