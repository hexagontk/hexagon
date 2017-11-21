package com.hexagonkt.store

import java.net.URL
import java.nio.ByteBuffer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

import org.testng.annotations.Test
import java.io.File

@Test class MongoRepositoryTest {
    data class Company(
        val id: String,
        val foundation: LocalDate,
        val closeTime: LocalTime,
        val openTime: ClosedRange<LocalTime>,
        val web: URL?,
        val clients: List<URL> = listOf(),
        val logo: ByteBuffer? = null,
        val notes: String? = null,
        val creationDate: LocalDateTime = LocalDateTime.now()
    )

    val repository = mongoIdRepository(Company::id)

    // TODO Check inserted data
    fun resources_are_loaded() {
        repository.loadData("companies.json")
        repository.delete()
        repository.importResource("companies.json")
        repository.delete()

        // File paths change from IDE to build tool
        val file = File("hexagon_core/src/test/resources/data/companies.json").let {
            if (it.exists()) it
            else File("src/test/resources/companies.json")
        }

        repository.importFile(file)
        repository.delete()
    }
}
