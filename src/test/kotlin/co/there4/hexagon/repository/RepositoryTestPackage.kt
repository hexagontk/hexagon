package co.there4.hexagon.repository

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
    val openTime: LocalTime,
    val web: URL?,
    val clients: List<URL> = listOf(),
    val logo: ByteBuffer? = null,
    val notes: String? = null,
    val people: Set<Person>,
    val departments: Set<Department> = setOf(),
    val creationDate: LocalDateTime = LocalDateTime.now()
)

data class Tag(
    val id: String = mongoId(),
    val name: String
)
