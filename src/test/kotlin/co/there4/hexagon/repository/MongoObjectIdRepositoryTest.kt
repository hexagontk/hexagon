package co.there4.hexagon.repository

import co.there4.hexagon.helpers.err
import org.testng.annotations.Test

@Test class MongoObjectIdRepositoryTest {
    data class Color(val id: String = mongoId(), val r: Int, val g: Int, val b: Int, val a: Int)

    fun repository_callbacks_works_ok() {
        val repository = MongoObjectIdRepository(
            Color::class,
            Color::id,
            onStore = {
                it[Color::r.name] = 0xFF
                it
            },
            onLoad = {
                it[Color::g.name] = 0xFF
                it
            }
        )

        val newColor = Color(r = 1, g = 2, b = 3, a = 0)
        repository.insertOneObject(newColor)
        val color = repository.find(newColor.id) ?: err
        assert(color.r == 0xFF)
        assert(color.g == 0xFF)
        repository.delete()
    }
}
