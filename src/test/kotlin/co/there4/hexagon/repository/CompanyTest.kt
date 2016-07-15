package co.there4.hexagon.repository

import co.there4.hexagon.repository.Department.*
import java.net.URL
import java.nio.ByteBuffer
import java.time.LocalDateTime

class CompanyTest :
    IdRepositoryTest<Company, String> (Company::class, "id", String::class, { it.id }) {
    override val testObjects: List<Company> = listOf (
        createObject(),
        Company(
            id = "id1",
            web = URL("http://example.org"),
            people = setOf(
                Person(name = "John"),
                Person(name = "Mike")
            )
        )
    )

    override fun setObjectKey(obj: Company, id: Int) = obj.copy (id = id.toString ())

    override fun changeObject(obj: Company) = obj.copy (notes = obj.notes + "_modified")

    override fun createObject() = Company (
        id = "id",
        web = URL ("http://example.org"),
        clients = listOf (
            URL ("http://c1.example.org"),
            URL ("http://c2.example.org")
        ),
        logo = ByteBuffer.wrap (byteArrayOf (0, 1, 2)),
        notes = "notes",
        people = setOf (
            Person (name = "John"),
            Person (name = "Mike")
        ),
        departments = setOf (DESIGN, DEVELOPMENT),
        creationDate = LocalDateTime.of (2016, 1, 1, 0, 0, 0)
    )
}
