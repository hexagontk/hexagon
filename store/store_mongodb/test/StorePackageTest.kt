package com.hexagontk.store.mongodb

import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName
import java.net.URL
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
    val logo: ByteArray? = null,
    val notes: String? = null,
    val people: Set<Person> = setOf(),
    val departments: Set<Department> = setOf(),
    val creationDate: LocalDateTime = LocalDateTime.now().truncatedTo(MILLIS)
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Company

        if (id != other.id) return false
        if (!foundation.equals(other.foundation)) return false
        if (!closeTime.equals(other.closeTime)) return false
        if (!openTime.equals(other.openTime)) return false
        if (web != other.web) return false
        if (clients != other.clients) return false
        if (logo != null) {
            if (other.logo == null) return false
            if (!logo.contentEquals(other.logo)) return false
        } else if (other.logo != null) return false
        if (notes != other.notes) return false
        if (people != other.people) return false
        if (departments != other.departments) return false
        if (!creationDate.equals(other.creationDate)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + foundation.hashCode()
        result = 31 * result + closeTime.hashCode()
        result = 31 * result + openTime.hashCode()
        result = 31 * result + (web?.hashCode() ?: 0)
        result = 31 * result + clients.hashCode()
        result = 31 * result + (logo?.contentHashCode() ?: 0)
        result = 31 * result + (notes?.hashCode() ?: 0)
        result = 31 * result + people.hashCode()
        result = 31 * result + departments.hashCode()
        result = 31 * result + creationDate.hashCode()
        return result
    }
}

internal val mongoDb: MongoDBContainer by lazy {
    MongoDBContainer(DockerImageName.parse("mongo:8.0-noble"))
        .withExposedPorts(27017)
        .apply { start() }
}
