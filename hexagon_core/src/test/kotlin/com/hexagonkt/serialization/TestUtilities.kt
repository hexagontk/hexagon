package com.hexagonkt.serialization

import org.testng.annotations.Test
import java.net.URL
import java.nio.ByteBuffer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.LocalTime.MIDNIGHT

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
    override val testObjects: List<Company> = listOf(
        Company(
            "id",
            LocalDate.now(),
            MIDNIGHT,
            MIDNIGHT.. MIDNIGHT,
            URL("http://example.com"),
            people = setOf()
        )
    )
}
