package com.hexagonkt.serialization

import com.hexagonkt.helpers.toStream
import org.testng.annotations.Test
import java.net.URL
import java.nio.ByteBuffer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class SerializationPackageTest {

    private val company = Company(
        id = "id",
        foundation = LocalDate.of(2014, 1, 25),
        closeTime = LocalTime.of(11, 42),
        openTime = LocalTime.of(8, 30)..LocalTime.of(14, 51),
        web = URL("http://example.org"),
        clients = listOf(
            URL("http://c1.example.org"),
            URL("http://c2.example.org")
        ),
        logo = ByteBuffer.wrap(byteArrayOf(0, 1, 2)),
        notes = "notes",
        people = setOf(
            Person(name = "John"),
            Person(name = "Mike")
        ),
        departments = setOf(Department.DESIGN, Department.DEVELOPMENT),
        creationDate = LocalDateTime.of(2016, 1, 1, 0, 0, 0)
    )

    @Test fun `Inline convert object work correctly`() {
        val map = company.convertToMap()
        val obj = map.convertToObject<Company>()
        assert(company == obj)
        assert(company !== obj)
    }

    @Test fun `Inline convert list work correctly`() {
        val objects = listOf(
            company.copy(id = "id1"),
            company.copy(id = "id2")
        )
        val maps = objects.map { it.convertToMap() }
        val objects2 = maps.convertToObjects<Company>()

        assert(objects == objects2)
        assert(objects !== objects2)
    }

    @Test fun `Parse streams works properly`() {
        val json = """{ "foo" : "bar", "baz" : 0 }"""
        val m1 = json.toStream().parse(Map::class)
        val m2 = json.toStream().parse(Map::class, Json)

        assert(m1 == m2)
        assert(m1 !== m2)
    }

    @Test fun `Parse stream objects works properly`() {
        val json = """[ { "foo" : "bar" }, { "foo" : "baz" } ]"""
        val m1 = json.toStream().parseObjects(Map::class)
        val m2 = json.toStream().parseObjects(Map::class, Json)

        assert(m1 == m2)
        assert(m1 !== m2)
    }
}
