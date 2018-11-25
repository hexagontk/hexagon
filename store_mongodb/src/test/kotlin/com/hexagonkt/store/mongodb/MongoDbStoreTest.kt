package com.hexagonkt.store.mongodb

import com.hexagonkt.helpers.error
import com.hexagonkt.settings.SettingsManager
import com.hexagonkt.store.Store
import com.mongodb.ConnectionString
import com.mongodb.MongoClientURI
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import org.bson.types.ObjectId
import org.testng.annotations.Test
import java.net.URL
import java.time.LocalDate
import java.time.LocalTime

/**
 * TODO .
 */
@Test class MongoDbStoreTest {

    private val mongodbUrl = SettingsManager.settings["mongodbUrl"] as? String?
        ?: "mongodb://localhost/test"

    private val database: String = MongoClientURI(mongodbUrl).database ?: error
    private val db: MongoDatabase =
        MongoClients.create(ConnectionString(mongodbUrl)).getDatabase(database)
    private val store: Store<Company, String> =
        MongoDbStore(Company::class, Company::id, "companies", db)

    private fun createTestEntities() = (0..9)
        .map { ObjectId().toHexString() }
        .map {
            Company(
                id = it,
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

//    @BeforeClass fun `Drop collection`() {
//        store.drop()
//    }

    @Test fun `New records are stored`() {
        val id = ObjectId().toHexString()
        val company = Company(
            id = id,
            foundation = LocalDate.of(2014, 1, 25),
            closeTime = LocalTime.of(11, 42),
            openTime = LocalTime.of(8, 30)..LocalTime.of(14, 36),
            web = URL("http://example.org"),
            people = setOf(
                Person(name = "John"),
                Person(name = "Mike")
            )
        )

        store.insertOne(company)
        val storedCompany = store.findOne(id)
        assert(storedCompany == company)

        val changedCompany = company.copy(web = URL("http://change.example.org"))
        assert(store.replaceOne(changedCompany))
        val storedModifiedCompany = store.findOne(id)
        assert(storedModifiedCompany == changedCompany)
    }

    @Test fun `Many records are stored`() {
        val companies = (0..9)
            .map { ObjectId().toHexString() }
            .map {
                Company(
                    id = it,
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

        val keys = store.insertMany(*companies.toTypedArray())

        for (key in keys)
            println(key)
    }

    @Test(enabled = false) fun `Entities are stored`() {
        val testEntities = createTestEntities()
        store.drop()

        store.saveMany(testEntities)

        testEntities.forEach {
            store.saveOne(it)
        }

        // TODO Fix this!
//        store.saveMany(testEntities).await()
    }

    @Test fun `Insert one record returns the proper key`() {
        // TODO Do with MappedClass
        val id = ObjectId().toHexString()
        val mappedClass = Company(
            id = id,
            foundation = LocalDate.of(2014, 1, 25),
            closeTime = LocalTime.of(11, 42),
            openTime = LocalTime.of(8, 30)..LocalTime.of(14, 36),
            web = URL("http://example.org"),
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
}
