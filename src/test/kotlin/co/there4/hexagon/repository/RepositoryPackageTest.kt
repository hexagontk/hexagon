package co.there4.hexagon.repository

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

/**
 * TODO Finish this
 */
@Test(enabled = false, description = "TODO Compare both filters") class RepositoryPackageTest {
    private fun equalFilter(a: Bson, b: Bson) = false

    fun eq_filter_work_as_expected() {
        val hexagonFilter = "foo" eq "bar"
        val driverFilter = eq("foo", "bar")

        assert(equalFilter(hexagonFilter, driverFilter))
    }

    fun or_filter_work_as_expected() {
        val hexagonFilter = ("foo" eq "bar") or ("foo" eq true) or ("foo" eq 1)
        val driverFilter = or(
            eq("foo", "bar"),
            eq("foo", true),
            eq("foo", 1)
        )

        assert(hexagonFilter == driverFilter)
    }
}
