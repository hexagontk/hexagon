package co.there4.hexagon.store

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Filters.*
import org.bson.conversions.Bson
import org.testng.annotations.Test
import java.net.URL
import java.nio.ByteBuffer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class Department { DESIGN, DEVELOPMENT }

data class Person(val name: String)

data class Company(
    val id: String,
    val foundation: LocalDate,
    val closeTime: LocalTime,
    val openTime: ClosedRange<LocalTime>,
    val web: URL?,
    val clients: List<URL> = listOf(),
    val logo: ByteBuffer? = null,
    val notes: String? = null,
    val people: Set<Person>,
    val departments: Set<Department> = setOf(),
    val creationDate: LocalDateTime = LocalDateTime.now()
)

data class Tag(
    val id: String = mongoId(),
    val name: String
)

@Test class RepositoryPackageTest {
    data class Example(val foo: String, var bar: Int)

    private infix fun Bson.sameAs(other: Bson) = this.toString() == other.toString()

    fun comparison_filters_work_as_expected() {
        assert(Example::foo eq "bar" sameAs eq("foo", "bar"))
        assert(Example::bar gte 1 sameAs gte("bar", 1))
        assert(Example::bar gt 1 sameAs gt("bar", 1))
        assert(Example::bar lte 1 sameAs lte("bar", 1))
        assert(Example::bar lt 1 sameAs lt("bar", 1))
        assert(Example::bar isIn listOf(1, 2) sameAs Filters.`in`("bar", listOf(1, 2)))
    }
}
