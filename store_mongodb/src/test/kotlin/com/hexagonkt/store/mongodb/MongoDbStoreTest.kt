package com.hexagonkt.store.mongodb

import com.mongodb.ConnectionString
import com.mongodb.MongoClientURI
import com.mongodb.async.client.MongoClients
import kotlinx.coroutines.experimental.async
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
    fun `New records are stored`() {
        val db = MongoClients.create(ConnectionString(mongodbUrl))
            .getDatabase(MongoClientURI(mongodbUrl).database)
        val store = MongoDbStore(Company::class, Company::id, "companies", db)

        runBlocking {
        println(async {
            store.insertOne(
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
        }.await())
        }
    }
}
