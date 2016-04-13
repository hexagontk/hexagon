package co.there4.hexagon.repository

import co.there4.hexagon.repository.Department.*
import java.net.URL
import java.nio.ByteBuffer
import java.time.LocalDateTime

class CompanyTest :
    IdRepositoryTest<Company, String> (Company::class, "id", String::class, { it.id }) {
    override val testObjects: List<Company> = listOf (
        Company (
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
    )

    override fun setObjectKey(obj: Company, id: Int) = obj.copy (id = id.toString ())

    override fun changeObject(source: Company) = source.copy (notes = source.notes + "_modified")

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
