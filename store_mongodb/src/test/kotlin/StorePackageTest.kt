package com.hexagonkt.store.mongodb

import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName
import java.net.URL
import java.nio.ByteBuffer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

import java.time.temporal.ChronoUnit.MILLIS

internal enum class Department { DESIGN, DEVELOPMENT }

internal data class Person(val name: String)

internal data class Company(
    val id: String,
    val foundation: LocalDate,
    val closeTime: LocalTime,
    val openTime: ClosedRange<LocalTime>,
    val web: URL?,
    val clients: List<URL> = listOf(),
    val logo: ByteBuffer? = null,
    val notes: String? = null,
    val people: Set<Person> = setOf(),
    val departments: Set<Department> = setOf(),
    val creationDate: LocalDateTime = LocalDateTime.now().truncatedTo(MILLIS)
)

internal val mongoDb: MongoDBContainer = MongoDBContainer(DockerImageName.parse("mongo:4.4-bionic"))
    .withExposedPorts(27017)
    .apply { start() }
