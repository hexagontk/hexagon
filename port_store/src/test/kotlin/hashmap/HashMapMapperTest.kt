package com.hexagonkt.store.hashmap

import com.hexagonkt.serialization.JacksonMapper
import com.hexagonkt.serialization.SerializationManager
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.reflect.full.declaredMemberProperties

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class HashMapMapperTest {

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
        val onePlus: Char = 'c',
        val localDate: LocalDate = LocalDate.MIN,
        val localDateTime: LocalDateTime = LocalDateTime.MIN
    )

    @BeforeAll fun setUpSerializationManager() {
        SerializationManager.mapper = JacksonMapper
    }

    @Test fun `A mapper transform a data class to a map and back`() {
        val instance = MappedClass()
        val mapper = HashMapMapper(MappedClass::class)
        val map = mapper.toStore(instance)

        val fieldNames = instance::class.declaredMemberProperties.map { it.name }
        assert(fieldNames.all { mapper.fields.containsKey(it) })
        assert(instance == mapper.fromStore(map))
        assert(LocalDate.MIN == mapper.fromStore("localDate", LocalDate.MIN))
        assert(LocalDateTime.MIN == mapper.fromStore("localDateTime", LocalDateTime.MIN))
    }
}
