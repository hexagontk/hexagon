package com.hexagonkt.store.mongodb

import com.hexagonkt.serialization.JacksonMapper
import com.hexagonkt.serialization.SerializationManager
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.util.Date
import kotlin.test.assertFailsWith

@TestInstance(PER_CLASS)
internal class MongoDbMapperTest {

    data class MappedClass (
        val oneString: String = "String",
        val oneBoolean: Boolean = true,
        val anInt: Int = 42,
        val oneLong: Long = 1_234L,
        val oneFloat: Float = 1.23F,
        val oneDouble: Double = 2.345,
        val oneList: List<String> = listOf("One", "Two"),
        val oneMap: Map<String, *> = mapOf("One" to 1, "Two" to true, "Three" to 0.12),
        val oneNullable: String? = null,
        val otherData: String = "other",
        val atHome: Int = 0,
        val onePlus: Char = 'c'
    )

    @BeforeAll fun setUpSerializationManager() {
        SerializationManager.mapper = JacksonMapper
    }

    @Test fun `A mapper transform a data class to a map and back`() {
        val instance = MappedClass()
        val mapper = MongoDbMapper(MappedClass::class, MappedClass::oneString)
        val map = mapper.toStore(instance)

        assert(instance == mapper.fromStore(map))
    }

    @Test fun `Mapping a date to an invalid field type results in error`() {
        assertFailsWith<IllegalStateException> {
            val mapper = MongoDbMapper(MappedClass::class, MappedClass::oneString)
            mapper.fromStore("onePlus", Date())
        }
    }
}
