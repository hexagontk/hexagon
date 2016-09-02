package co.there4.hexagon.repository

import org.testng.annotations.Test

@Test class MongoRepositoryTest {
    val repository = MongoIdRepository(Company::class, { it.id }, String::class, "id")

    fun resources_are_loaded() {
        repository.importResource("data/companies.json")
    }
}
