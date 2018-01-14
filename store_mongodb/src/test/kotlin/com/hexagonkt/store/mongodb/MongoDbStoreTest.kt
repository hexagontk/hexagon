package com.hexagonkt.store.mongodb

import com.mongodb.ConnectionString
import com.mongodb.MongoClientURI
import com.mongodb.async.client.MongoClients
import com.mongodb.async.client.MongoDatabase
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.runBlocking
import org.bson.types.ObjectId
import org.testng.annotations.Test
import java.net.URL
import java.time.LocalDate
import java.time.LocalTime

/**
 * TODO .
 */
@Test class MongoDbStoreTest {
    private val database: String = MongoClientURI(mongodbUrl).database
    private val db: MongoDatabase =
        MongoClients.create(ConnectionString(mongodbUrl)).getDatabase(database)
    private val store: MongoDbStore<Company, String> =
        MongoDbStore(Company::class, Company::id, "companies", db)

    fun `New records are stored`() = runBlocking {
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

        store.asyncInsertOne(company).await()
        val storedCompany = store.asyncFindOne(id).await()
        assert(storedCompany == company)

        val changedCompany = company.copy(web = URL("http://change.example.org"))
        assert(store.asyncReplaceOne(changedCompany).await())
        val storedModifiedCompany = store.asyncFindOne(id).await()
        assert(storedModifiedCompany == changedCompany)
    }

    fun `Many records are stored`() = runBlocking {
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

        val keys = store.insertMany(companies)

        for (key in keys)
            println(key)

        val channel = store.replaceMany(companies.map { it.copy(web = URL("http://changed.org")) })
        channel.consumeEach {
            println(it)
        }
    }
}
