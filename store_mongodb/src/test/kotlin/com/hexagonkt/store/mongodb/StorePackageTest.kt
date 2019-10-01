package com.hexagonkt.store.mongodb

import com.hexagonkt.helpers.error
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import com.mongodb.client.MongoDatabase
import java.net.URL
import java.nio.ByteBuffer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

import org.bson.types.ObjectId
import java.time.temporal.ChronoUnit.MILLIS

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
    val people: Set<Person> = setOf(),
    val departments: Set<Department> = setOf(),
    val creationDate: LocalDateTime = LocalDateTime.now().truncatedTo(MILLIS)
)

data class Tag(
    val id: String = ObjectId().toHexString(),
    val name: String
)

fun mongoDatabase (uri: String): MongoDatabase = MongoClientURI(uri).let {
    MongoClient(it).getDatabase(it.database ?: error)
}
