package com.hexagonkt.store

import org.testng.annotations.Test
import kotlin.reflect.KProperty1

@Test class MapperTest {

    @Test fun `Default mapper methods return its own parameters`() {
        val testMapper = object : Mapper<String> {
            override val fields: Map<String, KProperty1<String, *>> = emptyMap()
            override fun toStore(instance: String): Map<String, Any> = emptyMap()
            override fun fromStore(map: Map<String, Any>): String = ""
        }

        assert(testMapper.fromStore("property", "value") == "value")
        assert(testMapper.toStore("property", "value") == "value")
    }
}
