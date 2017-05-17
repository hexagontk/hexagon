package co.there4.hexagon.store

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

        // File paths change from IDE to build tool
        val file = File("core/src/test/resources/data/companies.json").let {
            if (it.exists()) it
            else File("src/test/resources/data/companies.json")
        }

        repository.importFile(file)
        repository.delete()
    }
}
