package com.hexagontk.store.mongodb

import com.hexagontk.core.fieldsMapOf
import com.hexagontk.core.getPath
import com.hexagontk.core.requirePath
import com.hexagontk.core.urlOf
import com.hexagontk.store.Store
import com.hexagontk.store.mongodb.Department.DESIGN
import com.hexagontk.store.mongodb.Department.DEVELOPMENT
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.condition.DisabledInNativeImage
import org.junit.jupiter.api.condition.DisabledOnOs
import org.junit.jupiter.api.condition.OS
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

@TestInstance(PER_CLASS)
@DisabledOnOs(OS.WINDOWS)
@DisabledInNativeImage // TODO Fix for native image
internal class CompanyTest : StoreTest<Company, String>() {
    private val company = Company(
        id = "id",
        foundation = LocalDate.of(2014, 1, 25),
        closeTime = LocalTime.of(11, 42),
        openTime = LocalTime.of(8, 30)..LocalTime.of(14, 51),
        web = urlOf("http://example.org"),
        clients = listOf(
            urlOf("http://c1.example.org"),
            urlOf("http://c2.example.org")
        ),
        logo = byteArrayOf(0, 1, 2),
        notes = "notes",
        people = setOf(
            Person(name = "John"),
            Person(name = "Mike")
        ),
        departments = setOf(DESIGN, DEVELOPMENT),
        creationDate = LocalDateTime.of(2016, 1, 1, 0, 0, 0)
    )

    private val company1 = Company(
        id = "id1",
        foundation = LocalDate.of(2014, 1, 25),
        closeTime = LocalTime.of(11, 42),
        openTime = LocalTime.of(8, 30)..LocalTime.of(14, 36),
        web = urlOf("http://example.org"),
        people = setOf(
            Person(name = "John"),
            Person(name = "Mike")
        )
    )

    override fun createTestEntities(): List<Company> = listOf(company, company1)

    private val mongodbUrl by lazy { "mongodb://localhost:${mongoDb.getMappedPort(27017)}/test" }

    override fun createStore(): Store<Company, String> =
        MongoDbStore(
            Company::class,
            Company::id,
            mongodbUrl,
            "companies",
            encoder = { c ->
                fieldsMapOf(
                    Company::id to c.id,
                    Company::foundation to c.foundation,
                    Company::closeTime to c.closeTime,
                    Company::openTime to fieldsMapOf(
                        ClosedRange<*>::start to c.openTime.start,
                        ClosedRange<*>::endInclusive to c.openTime.endInclusive,
                    ),
                    Company::web to c.web,
                    Company::clients to c.clients.map { it.toString() },
                    Company::logo to c.logo,
                    Company::notes to c.notes,
                    Company::people to c.people.map { mapOf(Person::name.name to it.name) },
                    Company::departments to c.departments,
                    Company::creationDate to c.creationDate,
                )
            },
            decoder = { m ->
                Company(
                    id = m.requirePath(Company::id),
                    foundation = m.requirePath<LocalDateTime>(Company::foundation).toLocalDate(),
                    closeTime = m.requirePath<LocalDateTime>(Company::closeTime).toLocalTime(),
                    openTime = m.requirePath<Map<*,*>>(Company::openTime).let { t ->
                        val s = t.requirePath<LocalDateTime>(ClosedRange<*>::start).toLocalTime()
                        val e = t.requirePath<LocalDateTime>(ClosedRange<*>::endInclusive).toLocalTime()
                        s..e
                    },
                    web = urlOf(m.requirePath(Company::web)),
                    clients = m.getPath<List<String>>(Company::clients)?.map { urlOf(it) } ?: emptyList(),
                    logo = m.getPath(Company::logo),
                    notes = m.getPath(Company::notes),
                    people = m.getPath<List<Map<*, *>>>(Company::people)
                        ?.map { Person(name = it.requirePath(Person::name.name)) }
                        ?.toSet()
                        ?: emptySet(),
                    departments = m.getPath<List<String>>(Company::departments)
                        ?.map { Department.valueOf(it) }
                        ?.toSet()
                        ?: emptySet(),
                    creationDate = m.requirePath(Company::creationDate),
                )
            }
        )

    override fun changeObject(obj: Company) =
        obj.copy(web = urlOf("http://change.example.org"))

    @BeforeAll fun initialize() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

//    @Test fun `New records are stored`() {
//        new_records_are_stored()
//    }
//
//    @Test fun `Many records are stored`() {
//        many_records_are_stored()
//    }

    @Test fun `Entities are stored`() {
        entities_are_stored()
    }

    @Test fun `Insert one record returns the proper key`() {
        insert_one_record_returns_the_proper_key()
    }
}
