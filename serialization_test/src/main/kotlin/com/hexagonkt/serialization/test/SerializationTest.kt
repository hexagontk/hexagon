package com.hexagonkt.serialization.test

import com.hexagonkt.core.requirePath
import com.hexagonkt.serialization.*
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
        // serializationUsage
        SerializationManager.formats = setOf(format) // Loads the serialization format
        val jason = Person("Jason", "Jackson", LocalDate.of(1989, 12, 31))

        val jasonJson = personToMap(jason).serialize(format)
        val parsedJason = mapToPerson(jasonJson.parseMap(format))

        assertEquals(parsedJason, jason)
        assert(jason !== parsedJason)
        // serializationUsage
    }

    private fun personToMap(person: Person): Map<*, *> =
        mapOf(
            "givenName" to person.givenName,
            "familyName" to person.familyName,
            "birthDate" to person.birthDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        )

    private fun mapToPerson(map: Map<*, *>) =
        Person(
            givenName = map.requirePath("givenName"),
            familyName = map.requirePath("familyName"),
            birthDate = LocalDate.parse(map.requirePath("birthDate"))
        )
}
