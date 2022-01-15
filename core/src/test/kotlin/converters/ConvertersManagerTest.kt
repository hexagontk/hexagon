package com.hexagonkt.core.converters

import com.hexagonkt.core.converters.ConvertersManager.convertObjects
import com.hexagonkt.core.keys
import com.hexagonkt.core.fail
import com.hexagonkt.core.requireKeys
import org.junit.jupiter.api.Test
import java.lang.IllegalStateException
import java.net.URL
import java.nio.ByteBuffer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

internal class ConvertersManagerTest {

    internal data class Person(val givenName: String, val familyName: String)

    internal data class Company(
        val id: String,
        val foundation: LocalDate,
        val closeTime: LocalTime,
        val openTime: ClosedRange<LocalTime>,
        val web: URL? = null,
        val clients: List<URL> = listOf(),
        val logo: ByteBuffer? = null,
        val notes: String? = null,
        val people: Set<Person> = emptySet(),
        val creationDate: LocalDateTime = LocalDateTime.now(),
    )

    @Test fun `Converters on null collections return emptyLists by default`() {
        assertEquals(emptyList(), null.convertObjects<Person>())
        @Suppress("KotlinConstantConditions") // Warning ignored for the sake of testing
        assertEquals(listOf(1), null?.convertObjects<Person>() ?: listOf(1))
    }

    @Test fun `Converters are searched by parent classes if no found in first place`() {

        ConvertersManager.register(Map::class to Person::class) {
            Person(
                it.requireKeys(Person::givenName::name),
                it.requireKeys(Person::familyName::name),
            )
        }

        val personData = mapOf(
            Person::givenName::name to "Johny",
            Person::familyName::name to "Cage",
        )

        val person = personData.convert<Person>()
        assertEquals(Person("Johny", "Cage"), person)
    }

    @Test fun `Convert instances to its own type returns the same instance`() {
        val source = Date()
        assertSame(source, ConvertersManager.convert(source, source::class))
    }

    @Test fun `Type conversion works properly for collections of instances`() {

        val millis = System.currentTimeMillis()
        val dates = listOf(Date(millis), Date(millis + 100))

        ConvertersManager.register(Date::class to String::class) { it.toString() }
        val convertedResults = listOf(

            convertObjects(dates, String::class),
            dates.convertObjects(String::class),
            dates.convertObjects(),

            convertObjects(dates.toSet(), String::class),
            dates.toSet().convertObjects(String::class),
            dates.toSet().convertObjects(),
        )

        convertedResults.forEach { assertEquals(convertedResults.first(), it) }

        ConvertersManager.remove(Date::class to String::class)
        val e = assertFailsWith<IllegalStateException> { dates.convertObjects<String>() }

        val source = Date::class.simpleName
        val target = String::class.simpleName
        assertEquals("No converter for $source -> $target", e.message)
    }

    @Test fun `Type conversion works properly`() {
        ConvertersManager.register(Date::class to String::class) { it.toString() }
        val dateText1 = ConvertersManager.convert(Date(), String::class)
        val dateText2 = Date().convert(String::class)
        val dateText3 = Date().convert<String>()
        assertEquals(dateText1, dateText2)
        assertEquals(dateText2, dateText3)
        ConvertersManager.remove(Date::class to String::class)
        val e = assertFailsWith<IllegalStateException> { Date().convert<String>() }
        val source = Date::class.simpleName
        val target = String::class.simpleName
        assertEquals("No converter for $source -> $target", e.message)
    }

    @Test fun `Nested type conversion works properly`() {
        ConvertersManager.register(Person::class to Map::class, ::personToMap)
        ConvertersManager.register(Company::class to Map::class, ::companyToMap)

        val date = LocalDate.now()
        val time = LocalTime.now()
        val openTime = time.minusMinutes(1)..time.plusMinutes(1)

        Company("1", date, time, openTime).let {
            val m: Map<String, *> = it.convert()
            assertEquals("1", m[Company::id.name])
        }

        Company("1", date, time, openTime, people = setOf(Person("John", "Smith"))).let {
            val m: Map<String, *> = it.convert()
            val persons = m.keys<Map<*, *>>(Company::people.name, 0) ?: fail
            assertEquals("John", persons[Person::givenName.name])
            assertEquals("Smith", persons[Person::familyName.name])
        }
    }

    @Test fun `Delete non existing key don't generate errors`() {
        ConvertersManager.remove(Int::class to Date::class)
    }

    @Test fun usageExample() {
        // Define a mapper from a source type to a target type
        ConvertersManager.register(Date::class to String::class) { it.toString() }

        // Conversions can be done with different utility methods
        val directConversion = ConvertersManager.convert(Date(), String::class)
        val utilityConversion = Date().convert(String::class)
        val reifiedUtilityConversion = Date().convert<String>()

        assertEquals(directConversion, utilityConversion)
        assertEquals(utilityConversion, reifiedUtilityConversion)

        // Conversion mappers can be deleted
        ConvertersManager.remove(Date::class to String::class)

        // Trying to perform a conversion that has not a registered mapper fails with an error
        val e = assertFailsWith<IllegalStateException> { Date().convert<String>() }
        val source = Date::class.simpleName
        val target = String::class.simpleName
        assertEquals("No converter for $source -> $target", e.message)
    }

    private fun personToMap(person: Person): Map<String, *> =
        mapOf(
            "givenName" to person.givenName,
            "familyName" to person.familyName,
        )

    private fun companyToMap(company: Company): Map<String, *> =
        mapOf(
            "id" to company.id,
            "foundation" to company.foundation,
            "closeTime" to company.closeTime,
            "openTime" to company.openTime,
            "web" to company.web,
            "clients" to company.clients,
            "logo" to company.logo,
            "notes" to company.notes,
            "people" to company.people.map { it.convert<Map<String, Any>>() }.toList(),
            "creationDate" to company.creationDate,
        )
}
