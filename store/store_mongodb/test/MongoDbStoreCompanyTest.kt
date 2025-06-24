package com.hexagontk.store.mongodb

import com.hexagontk.core.*
import com.hexagontk.store.Store
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
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
internal class MongoDbStoreCompanyTest {

    private val mongodbUrl by lazy { "mongodb://localhost:${mongoDb.getMappedPort(27017)}/test" }

    private val store: Store<Company, String> by lazy {
        createStore()
    }

    private fun createStore(): Store<Company, String> =
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
            },
        )

    private fun createTestEntities(): List<Company> = (0..9)
        .map {
            Company(
                id = ObjectId().toHexString(),
                foundation = LocalDate.of(2014, 1, 25),
                closeTime = LocalTime.of(11, 42),
                openTime = LocalTime.of(8, 30)..LocalTime.of(14, 36),
                web = urlOf("http://$it.example.org"),
                people = setOf(
                    Person(name = "John"),
                    Person(name = "Mike")
                )
            )
        }

    private fun changeObject(obj: Company) =
        obj.copy(web = urlOf("http://change.example.org"))

    @BeforeAll fun initialize() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @BeforeEach fun dropCollection() {
        store.drop()
    }

    @Test fun `Store type is correct`() {
        assert(store.type == Company::class)
    }

    @Test fun `New records are stored`() {

        createTestEntities().forEach { entity ->
            store.insertOne(entity)
            val storedCompany = store.findOne(entity.id)
            assert(storedCompany == entity)
            assert(store.findOne(ObjectId().toHexString()) == null)

            assert(store.replaceOne(entity)) // Ensures unmodified instance is also "replaced"
            val changedCompany = changeObject(entity)
            assert(store.replaceOne(changedCompany))
            val storedModifiedCompany = store.findOne(entity.id)
            assert(storedModifiedCompany == changedCompany)

            val key = changedCompany.id
            val fields = listOf("web")

            // Ensures unmodified instance is also "updated"
            assert(store.updateOne(key, mapOf("web" to changedCompany.web)))
            assert(store.updateOne(key, mapOf("web" to urlOf("http://update.example.org"))))
            assert(store.findOne(key, fields)?.get("web") == "http://update.example.org")

            val keyName = store.key.name
            assert(store.findOne(key, fields) == store.findOne(mapOf(keyName to key), fields))
            assert(store.findOne(key) == store.findOne(mapOf(keyName to key)))

            assert(store.updateOne(key,
                Company::web to urlOf("http://update1.example.org"),
                Company::foundation to LocalDate.of(2015, 1, 1),
                Company::creationDate to LocalDateTime.of(2015, 1, 1, 23, 59)
            ))
            store.findOne(key, fields + "foundation" + "creationDate")?.apply {
                assert(get("web") == "http://update1.example.org")
                assert((get("foundation") as? LocalDateTime)?.toLocalDate()
                    ?.equals(LocalDate.of(2015, 1, 1)) ?: (LocalDate.of(2015, 1, 1) == null))
                assert(get("creationDate")?.equals(LocalDateTime.of(2015, 1, 1, 23, 59)) ?: (LocalDateTime.of(2015, 1, 1, 23, 59) == null))
            }
            store.findOne(key)?.apply {
                assert(web?.equals(urlOf("http://update1.example.org")) ?: false)
                assert(foundation.equals(LocalDate.of(2015, 1, 1)))
                assert(creationDate.equals(LocalDateTime.of(2015, 1, 1, 23, 59)))
            }

            assert(store.count() == 1L)
            assert(store.deleteOne(key))
            assert(store.count() == 0L)
            assert(!store.replaceOne(entity))
            assert(!store.updateOne(key, mapOf("web" to urlOf("http://update.example.org"))))
            assert(!store.deleteOne(key))
            assert(store.findOne(key) == null)
            assert(store.findOne(key, listOf("web")) == null)
        }
    }

    @Test fun `Many records are stored`() {
        val companies = createTestEntities()

        val keys = store.insertMany(*companies.toTypedArray())
        assert(store.count() == companies.size.toLong())

        val changedCompanies = companies.map { changeObject(it) }
        assert(store.replaceMany(*changedCompanies.toTypedArray()).size == companies.size)
        assert(store.findAll(listOf("web")).all { it["web"] == "http://change.example.org" })

        // TODO Improve asserts of methods below
        checkFindAllObjects()
        checkFindAllFields()
        checkFindObjects()
        checkFindFields()

        val updateMany = store.updateMany(
            mapOf("id" to keys),
            mapOf("web" to "http://update.example.org")
        )
        assert(updateMany == keys.size.toLong())
        val updatedCompanies = store.findMany(mapOf("id" to keys))
        assert(updatedCompanies.all { it.web.toString() == "http://update.example.org" })

        assert(store.count(mapOf("id" to keys)) == companies.size.toLong())
        assert(store.deleteMany(mapOf("id" to keys)) == keys.size.toLong())
        assert(store.count() == 0L)
        assert(store.replaceMany(updatedCompanies).isEmpty())
    }

    @Test fun `Entities are stored`() {
        val testEntities = createTestEntities()

        val keys = store.saveMany(testEntities)

        val entities = keys.map { store.findOne(it ?: fail) }

        assert(entities.map { it?.id }.all { it in keys })

        store.saveMany(testEntities.map { changeObject(it) })
    }

    @Test fun `Insert one record returns the proper key`() {

        createTestEntities().forEach { mappedClass ->
            val await = store.insertOne(mappedClass)
            val storedClass = store.findOne(await)
            assert(await.isNotBlank())
            assert(mappedClass == storedClass)
        }
    }

    @Test fun `Collection can be used for custom queries`() {
        store.insertMany(
            listOf(
                Company(
                    id = ObjectId().toHexString(),
                    foundation = LocalDate.of(2014, 1, 25),
                    closeTime = LocalTime.of(11, 42),
                    openTime = LocalTime.of(8, 30)..LocalTime.of(14, 36),
                    web = urlOf("http://example.org"),
                    people = setOf(
                        Person(name = "John"),
                        Person(name = "Mike")
                    )
                )
            )
        )

        assert((store as MongoDbStore<Company, String>).collection.countDocuments() == 1L)
    }

    private fun checkFindAllObjects() {
        val results = store.findAll(4, 8, mapOf("id" to false))
        assert(results.size == 2)
        assert(results.all { it.web == urlOf("http://change.example.org") })

        val results3 = store.findAll(limit = 4, sort = mapOf("id" to false))
        assert(results3.size == 4)
        assert(results3.all { it.web == urlOf("http://change.example.org") })

        val results4 = store.findAll(skip = 4, sort = mapOf("id" to false))
        assert(results4.size == 6)
        assert(results4.all { it.web == urlOf("http://change.example.org") })

        val results5 = store.findAll(sort = mapOf("id" to false))
        assert(results5.size == 10)
        assert(results5.all { it.web == urlOf("http://change.example.org") })

        val results6 = store.findAll()
        assert(results6.size == 10)
        assert(results6.all { it.web == urlOf("http://change.example.org") })
    }

    private fun checkFindAllFields() {
        val fields = listOf("id", "web")

        val results = store.findAll(fields, 4, 8, mapOf("id" to false))
        assert(results.size == 2)
        assert(results.all { it["web"] == "http://change.example.org" })

        val results3 = store.findAll(fields, limit = 4, sort = mapOf("id" to false))
        assert(results3.size == 4)
        assert(results3.all { it["web"] == "http://change.example.org" })

        val results4 = store.findAll(fields, skip = 4, sort = mapOf("id" to false))
        assert(results4.size == 6)
        assert(results4.all { it["web"] == "http://change.example.org" })

        val results5 = store.findAll(fields, sort = mapOf("id" to false))
        assert(results5.size == 10)
        assert(results5.all { it["web"] == "http://change.example.org" })

        val results6 = store.findAll(fields)
        assert(results6.size == 10)
        assert(results6.all { it["web"] == "http://change.example.org" })
    }

    private fun checkFindObjects() {
        val filter = emptyMap<String, Any>()

        val results = store.findMany(filter, 4, 8, mapOf("id" to false))
        assert(results.size == 2)
        assert(results.all { it.web == urlOf("http://change.example.org") })

        val results3 = store.findMany(filter, limit = 4, sort = mapOf("id" to false))
        assert(results3.size == 4)
        assert(results3.all { it.web == urlOf("http://change.example.org") })

        val results4 = store.findMany(filter, skip = 4, sort = mapOf("id" to false))
        assert(results4.size == 6)
        assert(results4.all { it.web == urlOf("http://change.example.org") })

        val results5 = store.findMany(filter, sort = mapOf("id" to false))
        assert(results5.size == 10)
        assert(results5.all { it.web == urlOf("http://change.example.org") })

        val results6 = store.findMany(filter)
        assert(results6.size == 10)
        assert(results6.all { it.web == urlOf("http://change.example.org") })
    }

    private fun checkFindFields() {
        val fields = listOf("id", "web")
        val filter = emptyMap<String, Any>()

        val results = store.findMany(filter, fields, 4, 8, mapOf("id" to false))
        assert(results.size == 2)
        assert(results.all { it["web"] == "http://change.example.org" })

        val results3 = store.findMany(filter, fields, limit = 4, sort = mapOf("id" to false))
        assert(results3.size == 4)
        assert(results3.all { it["web"] == "http://change.example.org" })

        val results4 = store.findMany(filter, fields, skip = 4, sort = mapOf("id" to false))
        assert(results4.size == 6)
        assert(results4.all { it["web"] == "http://change.example.org" })

        val results5 = store.findMany(filter, fields, sort = mapOf("id" to false))
        assert(results5.size == 10)
        assert(results5.all { it["web"] == "http://change.example.org" })

        val results6 = store.findMany(filter, fields)
        assert(results6.size == 10)
        assert(results6.all { it["web"] == "http://change.example.org" })
    }
}
