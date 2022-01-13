package com.hexagonkt.serialization.test

import com.hexagonkt.core.converters.ConvertersManager
import com.hexagonkt.core.converters.convert
import com.hexagonkt.core.requireKeys
import com.hexagonkt.serialization.SerializationFormat
import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.serialization.parse
import com.hexagonkt.serialization.serialize
import org.junit.jupiter.api.Test
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals

@Suppress("FunctionName") // This class's functions are intended to be used only in tests
abstract class SerializationTest {

    internal data class Person(
        val givenName: String,
        val familyName: String,
        val birthDate: LocalDate
    )

    abstract val format: SerializationFormat
    abstract val urls: List<URL>

    @Test fun `Parse URLs works ok`() {
        SerializationManager.formats = setOf(format) // Loads the serialization format
        urls.forEach {
            val companies = it.parse()
            assertEquals(companies, companies.serialize(format).parse(format))
        }
    }

    @Test fun serializationUsage() {
        ConvertersManager.register(Person::class to Map::class) {
            mapOf(
                "givenName" to it.givenName,
                "familyName" to it.familyName,
                "birthDate" to it.birthDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            )
        }

        ConvertersManager.register(LinkedHashMap::class to Person::class) {
            Person(
                givenName = it.requireKeys("givenName"),
                familyName = it.requireKeys("familyName"),
                birthDate = LocalDate.parse(it.requireKeys("birthDate"))
            )
        }

        // serializationUsage
        SerializationManager.formats = setOf(format) // Loads the serialization format
        val jason = Person("Jason", "Jackson", LocalDate.of(1989, 12, 31))

        val jasonJson = jason.convert<Map<*, *>>().serialize(format)
        val parsedJason = jasonJson.parse(format).convert<Person>()

        assertEquals(parsedJason, jason)
        assert(jason !== parsedJason)
        // serializationUsage
    }
}
