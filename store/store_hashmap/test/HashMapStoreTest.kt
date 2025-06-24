package com.hexagontk.store.hashmap

import com.hexagontk.core.*
import com.hexagontk.store.Store
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class HashMapStoreTest {

    private val store: Store<Company, String> =
        HashMapStore(
            Company::class,
            Company::id,
            "companies",
            encoder = { c ->
                fieldsMapOf(
                    Company::id to c.id,
                    Company::foundation to c.foundation,
                    Company::closeTime to c.closeTime,
                    Company::openTime to c.openTime,
                    Company::web to c.web,
                    Company::clients to c.clients,
                    Company::logo to c.logo,
                    Company::notes to c.notes,
                    Company::people to c.people,
                    Company::departments to c.departments,
                    Company::creationDate to c.creationDate,
                )
            },
            decoder = { m ->
                Company(
                    id = m.requirePath(Company::id.name),
                    foundation = m.requirePath(Company::foundation.name),
                    closeTime = m.requirePath(Company::closeTime.name),
                    openTime = m.requirePath(Company::openTime.name),
                    web = m.requirePath(Company::web.name),
                    clients = m.getPath(Company::clients) ?: emptyList(),
                    logo = m.getPath(Company::logo),
                    notes = m.getPath(Company::notes),
                    people = m.getPath(Company::people) ?: emptySet(),
                    departments = m.getPath(Company::departments) ?: emptySet(),
                    creationDate = m.requirePath(Company::creationDate),
                )
            }
        )

    @BeforeEach fun dropCollection() {
        store.drop()
    }

    @Test fun `Store type is correct`() {
        assert(store.type == Company::class)
    }

    @Test fun `Records are replaced correctly`() {
        val originalCompany = Company(
            id = "1",
            foundation = LocalDate.of(2014, 1, 25),
            closeTime = LocalTime.of(11, 42),
            openTime = LocalTime.of(8, 30)..LocalTime.of(14, 36),
            web = urlOf("http://example.org"),
            people = setOf(
                Person(name = "John"),
                Person(name = "Mike")
            )
        )

        store.insertOne(originalCompany)
        assert(originalCompany == store.findOne("1"))

        val replacementCompany = Company(
            id = "1",
            foundation = LocalDate.of(2019, 11, 1),
            closeTime = LocalTime.of(11, 42),
            openTime = LocalTime.of(8, 30)..LocalTime.of(14, 36),
            web = urlOf("http://new-example.org"),
            people = setOf(
                Person(name = "Jane"),
                Person(name = "James")
            )
        )

        val differentCompany = Company(
            id = "2",
            foundation = LocalDate.of(2019, 11, 1),
            closeTime = LocalTime.of(11, 42),
            openTime = LocalTime.of(8, 30)..LocalTime.of(14, 36),
            web = urlOf("http://other-example.org"),
            people = setOf(
                Person(name = "Albert"),
                Person(name = "Paula")
            )
        )

        assert(store.replaceOne(replacementCompany))
        assert(!store.replaceOne(differentCompany))

        val replaceMany = store.replaceMany(listOf(originalCompany, differentCompany))

        assert(replaceMany.size == 1)
        assert(replaceMany.first().id == "1")
        assert(replaceMany.first().web.toString() == "http://example.org")
    }

    @Test fun `New records are stored`() {
        val id = 1.toString()
        val company = Company(
            id = id,
            foundation = LocalDate.of(2014, 1, 25),
            closeTime = LocalTime.of(11, 42),
            openTime = LocalTime.of(8, 30)..LocalTime.of(14, 36),
            web = urlOf("http://example.org"),
            people = setOf(
                Person(name = "John"),
                Person(name = "Mike")
            )
        )

        store.insertOne(company)
        val storedCompany = store.findOne(id)
        assert(storedCompany == company)
        assert(store.findOne(2.toString()) == null)

        assert(store.replaceOne(company)) // Ensures unmodified instance is also "replaced"
        val changedCompany = company.copy(web = urlOf("http://change.example.org"))
        assert(store.replaceOne(changedCompany))
        val storedModifiedCompany = store.findOne(id)
        assert(storedModifiedCompany == changedCompany)

        val key = changedCompany.id
        val fields = listOf("web")

        // Ensures unmodified instance is also "updated"
        assert(store.updateOne(key, mapOf("web" to changedCompany.web)))
        assert(store.updateOne(key, mapOf("web" to urlOf("http://update.example.org"))))
        assert(store.findOne(key, fields)?.get("web").toString() == "http://update.example.org")

        assert(store.findOne(key, fields) == store.findOne(mapOf(store.key.name to key), fields))
        assert(store.findOne(key) == store.findOne(mapOf(store.key.name to key)))

        assert(store.updateOne(key,
            Company::web to urlOf("http://update1.example.org"),
            Company::foundation to LocalDate.of(2015, 1, 1),
            Company::creationDate to LocalDateTime.of(2015, 1, 1, 23, 59)
        ))
        store.findOne(key, fields + "foundation" + "creationDate")?.apply {
            assert(get("web").toString() == "http://update1.example.org")
            assert(get("foundation")?.equals(LocalDate.of(2015, 1, 1)) ?: (LocalDate.of(2015, 1, 1) == null))
            assert(get("creationDate")?.equals(LocalDateTime.of(2015, 1, 1, 23, 59)) ?: (LocalDateTime.of(2015, 1, 1, 23, 59) == null))
        }

        assert(store.count() == 1L)
        assert(store.deleteOne(key))
        assert(store.count() == 0L)
        assert(!store.replaceOne(company))
        assert(!store.updateOne(key, mapOf("web" to urlOf("http://update.example.org"))))
        assert(!store.deleteOne(key))
        assert(store.findOne(key) == null)
        assert(store.findOne(key, listOf("web")) == null)
    }

    @Test fun `Many records are stored`() {
        val companies = (0..9)
            .map {
                Company(
                    id = it.toString(),
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

        val keys = store.insertMany(*companies.toTypedArray())
        assert(store.count() == companies.size.toLong())

        val changedCompanies = companies.map { it.copy(web = urlOf("http://change.example.org")) }
        assert(store.replaceMany(*changedCompanies.toTypedArray()).size == companies.size)
        val results = store.findAll(listOf("web"))
        assert(results.all { it["web"] == urlOf("http://change.example.org") })

        // TODO Improve asserts of methods below
        checkFindAllObjects()
        checkFindAllFields()
        checkFindObjects()
        checkFindFields()

        val updateMany = store.updateMany(mapOf("id" to keys), mapOf("web" to urlOf("http://update.example.org")))
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

        store.saveMany(testEntities.map { it.copy(web = urlOf(it.web.toString() + "/modified")) })
    }

    @Test fun `Insert one record returns the proper key`() {
        // TODO Do with MappedClass
        val id = 1.toString()
        val mappedClass = Company(
            id = id,
            foundation = LocalDate.of(2014, 1, 25),
            closeTime = LocalTime.of(11, 42),
            openTime = LocalTime.of(8, 30)..LocalTime.of(14, 36),
            web = urlOf("http://example.org"),
            people = setOf(
                Person(name = "John"),
                Person(name = "Mike")
            )
        )

        val await = store.insertOne(mappedClass)
        val storedClass = store.findOne(await)
        assert(await.isNotBlank())
        assert(mappedClass == storedClass)
    }

    private fun createTestEntities() = (0..9)
        .map {
            Company(
                id = it.toString(),
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
        assert(results.all { it["web"] == urlOf("http://change.example.org") })

        val results3 = store.findAll(fields, limit = 4, sort = mapOf("id" to false))
        assert(results3.size == 4)
        assert(results3.all { it["web"] == urlOf("http://change.example.org") })

        val results4 = store.findAll(fields, skip = 4, sort = mapOf("id" to false))
        assert(results4.size == 6)
        assert(results4.all { it["web"] == urlOf("http://change.example.org") })

        val results5 = store.findAll(fields, sort = mapOf("id" to false))
        assert(results5.size == 10)
        assert(results5.all { it["web"] == urlOf("http://change.example.org") })

        val results6 = store.findAll(fields)
        assert(results6.size == 10)
        assert(results6.all { it["web"] == urlOf("http://change.example.org") })
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
        assert(results.all { it["web"] == urlOf("http://change.example.org") })

        val results3 = store.findMany(filter, fields, limit = 4, sort = mapOf("id" to false))
        assert(results3.size == 4)
        assert(results3.all { it["web"] == urlOf("http://change.example.org") })

        val results4 = store.findMany(filter, fields, skip = 4, sort = mapOf("id" to false))
        assert(results4.size == 6)
        assert(results4.all { it["web"] == urlOf("http://change.example.org") })

        val results5 = store.findMany(filter, fields, sort = mapOf("id" to false))
        assert(results5.size == 10)
        assert(results5.all { it["web"] == urlOf("http://change.example.org") })

        val results6 = store.findMany(filter, fields)
        assert(results6.size == 10)
        assert(results6.all { it["web"] == urlOf("http://change.example.org") })
    }
}
