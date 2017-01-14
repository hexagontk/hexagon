package co.there4.hexagon.repository

import org.testng.annotations.Test
import java.io.File

@Test class MongoRepositoryTest {
    val repository = mongoIdRepository(Company::id)

    // TODO Check inserted data
    fun resources_are_loaded() {
        repository.loadData("data/companies.json")
        repository.delete()
        repository.importResource("data/companies.json")
        repository.delete()
        repository.importFile(File("src/test/resources/data/companies.json"))
        repository.delete()
    }
}
