package com.hexagonkt.serialization.jackson

import com.fasterxml.jackson.databind.MappingJsonFactory
import com.fasterxml.jackson.databind.node.*
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.hexagonkt.serialization.jackson.Department.DESIGN
import com.hexagonkt.serialization.jackson.Department.DEVELOPMENT
import com.hexagonkt.serialization.jackson.JacksonHelper.mapNode
import com.hexagonkt.serialization.jackson.JacksonHelper.nodeToCollection
import com.hexagonkt.serialization.jackson.TextFormat.serialize
import kotlin.test.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigInteger
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class JacksonHelperTest {

    @Test fun `Created mappers use custom codecs correctly`() {
        val mapper = JacksonHelper
            .createObjectMapper(MappingJsonFactory())
            .rebuild()
            .addModule(KotlinModule.Builder().build()) // Module only required for testing purposes
            .build()

        val jsonCompany =
            """
            {
              "id" : "id",
              foundation : "2014-01-25",
              closeTime : "11:42",
              openTime : {
                start : "08:30",
                endInclusive : "14:51:00"
              },
              "web" : "http://example.org",
              "clients" : [
                "http://c1.example.org",
                "http://c2.example.org"
              ],
              "logo" : "AAEC",
              "notes" : "notes",
              "people" : [
                { "name" : "John" },
                { "name" : "Mike" }
              ],
              "departments" : [ "DESIGN", "DEVELOPMENT" ],
              "creationDate" : "2016-01-01T00:00",
              "host" : "127.0.0.1",
              "averageMargin" : 0.13782355,
            }
            """

        val company = mapper.readValue(jsonCompany, Company::class.java)
        val serializedCompany = serialize(company.copy(notes = "${company.notes} updated"))
        val parsedCompany = mapper.readValue(serializedCompany, Company::class.java)

        assertEquals(setOf(DESIGN, DEVELOPMENT), company.departments)
        assertEquals(0.13782355F, company.averageMargin)
        assertEquals("notes updated", parsedCompany.notes)
    }

    @Test fun `All node types are correctly converted to JVM types`() {
        assertEquals("text", mapNode(TextNode("text")))
        assertEquals(BigInteger.TEN, mapNode(BigIntegerNode(BigInteger.TEN)))
        assertEquals(false, mapNode(BooleanNode.FALSE))
        assertEquals(0.1, mapNode(DoubleNode(0.1)))
        assertEquals(0.5F, mapNode(FloatNode(0.5F)))
        assertEquals(10, mapNode(IntNode(10)))
        assertEquals(100L, mapNode(LongNode(100L)))
        assertEquals(null, nodeToCollection(NullNode.instance))
        assertContentEquals(
            "bytes".toByteArray(),
            mapNode(BinaryNode("bytes".toByteArray())) as ByteArray
        )
    }
}
