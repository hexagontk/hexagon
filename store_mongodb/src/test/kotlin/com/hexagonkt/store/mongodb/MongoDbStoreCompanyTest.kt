package com.hexagonkt.store.mongodb

import com.hexagonkt.helpers.Resource
import com.hexagonkt.helpers.error
import com.hexagonkt.settings.SettingsManager
import com.hexagonkt.store.IndexOrder
import com.hexagonkt.store.Store
import org.bson.types.ObjectId
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Test class MongoDbStoreCompanyTest {

    private val mongodbUrl = SettingsManager.settings["mongodbUrl"] as? String?
        ?: "mongodb://localhost/test"

    private val store: Store<Company, String> by lazy {
        createStore()
    }

    private fun createStore(): Store<Company, String> =
        MongoDbStore(Company::class, Company::id, mongodbUrl, "companies")

    private fun createTestEntities(): List<Company> = (0..9)
        .map {
            Company(
                id = ObjectId().toHexString(),
                foundation = LocalDate.of(2014, 1, 25),
                closeTime = LocalTime.of(11, 42),
                openTime = LocalTime.of(8, 30)..LocalTime.of(14, 36),
                web = URL("http://$it.example.org"),
                people = setOf(
                    Person(name = "John"),
                    Person(name = "Mike")
                )
            )
        }

    private fun changeObject(obj: Company) =
        obj.copy(web = URL("http://change.example.org"))

    @BeforeMethod fun dropCollection() {
        store.drop()
        store.createIndex(true, store.key.name to IndexOrder.ASCENDING)
    }

    @Test fun `Store type is correct`() {
        assert(store.type == Company::class)
    }

    @Test fun `Indexes creation works ok`() {
        store.createIndex(true, Company::foundation.name to IndexOrder.DESCENDING)
        store.createIndex(true, Company::creationDate.name to IndexOrder.ASCENDING)
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
            assert(store.updateOne(key, mapOf("web" to URL("http://update.example.org"))))
            assert(store.findOne(key, fields)?.get("web") == "http://update.example.org")

            val keyName = store.key.name
            assert(store.findOne(key, fields) == store.findOne(mapOf(keyName to key), fields))
            assert(store.findOne(key) == store.findOne(mapOf(keyName to key)))

            assert(store.updateOne(key,
                Company::web to URL("http://update1.example.org"),
                Company::foundation to LocalDate.of(2015, 1, 1),
                Company::creationDate to LocalDateTime.of(2015, 1, 1, 23, 59)
            ))
            store.findOne(key, fields + "foundation" + "creationDate")?.apply {
                assert(get("web") == "http://update1.example.org")
                assert(get("foundation") == LocalDate.of(2015, 1, 1))
                assert(get("creationDate") == LocalDateTime.of(2015, 1, 1, 23, 59))
            }
            store.findOne(key)?.apply {
                assert(web == URL("http://update1.example.org"))
                assert(foundation == LocalDate.of(2015, 1, 1))
                assert(creationDate == LocalDateTime.of(2015, 1, 1, 23, 59))
            }

            assert(store.count() == 1L)
            assert(store.deleteOne(key))
            assert(store.count() == 0L)
            assert(!store.replaceOne(entity))
            assert(!store.updateOne(key, mapOf("web" to URL("http://update.example.org"))))
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

    private fun checkFindAllObjects() {
        val results = store.findAll(4, 8, mapOf("id" to false))
        assert(results.size == 2)
        assert(results.all { it.web == URL("http://change.example.org") })

        val results3 = store.findAll(limit = 4, sort = mapOf("id" to false))
        assert(results3.size == 4)
        assert(results3.all { it.web == URL("http://change.example.org") })

        val results4 = store.findAll(skip = 4, sort = mapOf("id" to false))
        assert(results4.size == 6)
        assert(results4.all { it.web == URL("http://change.example.org") })

        val results5 = store.findAll(sort = mapOf("id" to false))
        assert(results5.size == 10)
        assert(results5.all { it.web == URL("http://change.example.org") })

        val results6 = store.findAll()
        assert(results6.size == 10)
        assert(results6.all { it.web == URL("http://change.example.org") })
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
        assert(results.all { it.web == URL("http://change.example.org") })

        val results3 = store.findMany(filter, limit = 4, sort = mapOf("id" to false))
        assert(results3.size == 4)
        assert(results3.all { it.web == URL("http://change.example.org") })

        val results4 = store.findMany(filter, skip = 4, sort = mapOf("id" to false))
        assert(results4.size == 6)
        assert(results4.all { it.web == URL("http://change.example.org") })

        val results5 = store.findMany(filter, sort = mapOf("id" to false))
        assert(results5.size == 10)
        assert(results5.all { it.web == URL("http://change.example.org") })

        val results6 = store.findMany(filter)
        assert(results6.size == 10)
        assert(results6.all { it.web == URL("http://change.example.org") })
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

    @Test fun `Entities are stored`() {
        val testEntities = createTestEntities()

        val keys = store.saveMany(testEntities)

        val entities = keys.map { store.findOne(it ?: error) }

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
                    web = URL("http://example.org"),
                    people = setOf(
                        Person(name = "John"),
                        Person(name = "Mike")
                    )
                )
            )
        )

        assert((store as MongoDbStore<Company, String>).collection.countDocuments() == 1L)
    }

    // TODO Check inserted data
    @Test fun `Resources are loaded`() {
        store.import(Resource("companies.json"))
        store.drop()

        // File paths change from IDE to build tool
        val file = File("hexagon_core/src/test/resources/data/companies.json").let {
            if (it.exists()) it
            else File("src/test/resources/companies.json")
        }

        store.import(file)
        store.drop()
    }
}
