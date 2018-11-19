package com.hexagonkt.store.mongodb

import com.mongodb.client.MongoDatabase
import org.testng.annotations.Test
import java.io.File
import kotlin.reflect.KProperty1

@Test class MongoRepositoryTest {
    private val repository: MongoIdRepository<Company, String> = mongoIdRepository(Company::id)

    inline fun <reified T : Any, reified K : Any> mongoIdRepository(
        key: KProperty1<T, K>,
        database: MongoDatabase = mongoDatabase("mongodb://localhost/test"),
        indexOrder: Int? = 1) =
        MongoIdRepository (
            T::class,
            database.getCollection(T::class.simpleName ?: error("Error getting type name")),
            key,
            indexOrder
        )

    // TODO Check inserted data
    fun `Resources are loaded`() {
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
