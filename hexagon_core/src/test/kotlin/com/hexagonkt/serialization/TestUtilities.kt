package com.hexagonkt.serialization

import com.hexagonkt.serialization.Department.*
import org.testng.annotations.Test
import java.net.URL
import java.nio.ByteBuffer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class Department { DESIGN, DEVELOPMENT }

data class Person(val name: String)

data class Company(
    val id: String,
    val foundation: LocalDate,
    val closeTime: LocalTime,
    val openTime: ClosedRange<LocalTime>,
    val web: URL?,
    val clients: List<URL> = listOf(),
    val logo: ByteBuffer? = null,
    val notes: String? = null,
    val people: Set<Person>,
    val departments: Set<Department> = setOf(),
    val creationDate: LocalDateTime = LocalDateTime.now()
)

@Test class CompanySerializationTest : SerializationTest<Company>(Company::class) {
    private fun createObject() = Company(
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
        departments = setOf(DESIGN, DEVELOPMENT),
        creationDate = LocalDateTime.of(2016, 1, 1, 0, 0, 0)
    )

    override val testObjects: List<Company> = listOf (
        createObject(),
        Company(
            id = "id1",
            foundation = LocalDate.of(2014, 1, 25),
            closeTime = LocalTime.of(11, 42),
            openTime = LocalTime.of(8, 30)..LocalTime.of(14, 36),
            web = URL("http://example.org"),
            people = setOf(
                Person(name = "John"),
                Person(name = "Mike")
            )
        )
    )
}
