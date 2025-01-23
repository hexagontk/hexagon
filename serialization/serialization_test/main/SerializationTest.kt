package com.hexagontk.serialization.test

import com.hexagontk.core.getPath
import com.hexagontk.core.requirePath
import com.hexagontk.serialization.*
import org.junit.jupiter.api.Test
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals

@Suppress("FunctionName") // This class's functions are intended to be used only in tests
abstract class SerializationTest {

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

    @Test fun nullFields() {
        SerializationManager.formats = setOf(format)
        val jason = Group("Group", "Jackson")

        val jasonJson = groupToMap(jason).serialize(format)
        val parsedJason = mapToGroup(jasonJson.parseMap(format))

        assertEquals(parsedJason, jason)
        assert(jason !== parsedJason)
    }

    private fun personToMap(person: Person): Map<*, *> =
        mapOf(
            "givenName" to person.givenName,
            "familyName" to person.familyName,
            "birthDate" to person.birthDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        )

    private fun mapToPerson(map: Map<*, *>): Person =
        Person(
            givenName = map.requirePath("givenName"),
            familyName = map.requirePath("familyName"),
            birthDate = LocalDate.parse(map.requirePath("birthDate"))
        )

    private fun groupToMap(person: Group): Map<*, *> =
        mapOf(
            "name" to person.name,
            "admin" to person.admin,
        )

    private fun mapToGroup(map: Map<*, *>): Group =
        Group(
            name = map.requirePath("name"),
            admin = map.getPath("admin"),
        )
}
