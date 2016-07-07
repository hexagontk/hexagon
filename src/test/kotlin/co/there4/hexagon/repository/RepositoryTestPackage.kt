package co.there4.hexagon.repository

import java.net.URL
import java.nio.ByteBuffer
import java.time.LocalDateTime

enum class Department { DESIGN, DEVELOPMENT }

data class Person(val name: String)

data class Company(
    val id: String,
    val web: URL?,
    val clients: List<URL> = listOf(),
    val logo: ByteBuffer? = null,
    val notes: String? = null,
    val people: Set<Person>,
    val departments: Set<Department> = setOf(),
    val creationDate: LocalDateTime = LocalDateTime.now()
)
