package co.there4.hexagon.repository

import org.testng.annotations.Test

@Test class MongoRepositoryTest {
    val repository = mongoIdRepository(Company::id)

    fun resources_are_loaded() {
        repository.importResource("data/companies.json")
    }
}
