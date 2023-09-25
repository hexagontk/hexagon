package com.hexagonkt.serialization

import com.hexagonkt.core.decodeBase64
import com.hexagonkt.core.media.APPLICATION_PHP
import com.hexagonkt.core.media.APPLICATION_AVRO
import com.hexagonkt.core.urlOf
import com.hexagonkt.serialization.Department.DESIGN
import com.hexagonkt.serialization.Department.DEVELOPMENT
import com.hexagonkt.serialization.jackson.json.Json
import org.junit.jupiter.api.Test
import java.io.File
import java.net.InetAddress
import kotlin.IllegalStateException
import java.nio.ByteBuffer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class SerializationTest {

    @Test fun `Created data mappers work correctly`() {
        val jsonCompany =
            """
            {
              "id" : "id",
              foundation : "2014-01-25",
              closeTime : "11:42",
              openTime : {
                start : "08:30",
                end : "14:51:00"
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

        val company = jsonCompany.parseMap(Json).let { Company().copy(it) }
        val serializedCompany = company.copy(notes = "${company.notes} updated").serialize(Json)
        val parsedCompany = serializedCompany.parseMap(Json).let { Company().copy(it) }

        assertEquals(setOf(DESIGN, DEVELOPMENT), company.departments)
        assertEquals(0.13782355F, company.averageMargin)
        assertEquals("notes updated", parsedCompany.notes)
    }

    @Test fun `Data serialization helpers work properly`() {
        SerializationManager.formats = setOf(BinaryTestFormat)
        SerializationManager.defaultFormat = TextTestFormat

        assertContentEquals("text".toByteArray(), "text".serializeBytes(APPLICATION_PHP))
        assertContentEquals("text".serializeBytes(APPLICATION_PHP), "text".serializeBytes())
        assertContentEquals("text".toByteArray(), "text".serializeBytes(APPLICATION_AVRO))
        assertEquals("text", "text".serialize(APPLICATION_PHP))
        assertEquals("text".serialize(APPLICATION_PHP), "text".serialize())
        assertFailsWith<IllegalStateException> { "text".serialize(APPLICATION_AVRO) }

        assertEquals(listOf("text"), "string".parse(APPLICATION_PHP))
        assertEquals("string".parse(APPLICATION_PHP), "string".parse())
        assertEquals("string".parse(APPLICATION_PHP), "string".parse(TextTestFormat))
        assertEquals(listOf("bytes"), "string".parse(APPLICATION_AVRO))

        assertEquals("string".parse(TextTestFormat), "string".parse(APPLICATION_PHP))
        assertEquals("string".parse(BinaryTestFormat), "string".parse(APPLICATION_AVRO))

        assertEquals(listOf("text"), urlOf("classpath:data/company.php").parse())
        assertEquals(listOf("bytes"), urlOf("classpath:data/company.avro").parse())

        val resources = "src/test/resources"
        val phpFile = File("$resources/data/company.php").let {
            if (it.exists()) it
            else File("serialization/$resources/data/company.php")
        }
        val avroFile = File("$resources/data/company.avro").let {
            if (it.exists()) it
            else File("serialization/$resources/data/company.avro")
        }
        assertEquals(listOf("text"), phpFile.parse())
        assertEquals(listOf("bytes"), avroFile.parse())
        assertEquals(listOf("text"), phpFile.toPath().parse())
        assertEquals(listOf("bytes"), avroFile.toPath().parse())
    }

    @Test fun `Data serialization helpers convert data properly`() {
        SerializationManager.formats = setOf(Json)

        urlOf("classpath:data/company.json").parseMap().let { Company().copy(it) }.apply {
            assertEquals("id1", id)
            assertEquals(LocalDate.of(2014, 1, 25), foundation)
            assertEquals(LocalTime.of(11, 42), closeTime)
            assertEquals(LocalTime.of(8, 30)..LocalTime.of(14, 51), openTime)
            assertEquals(urlOf("http://example.org"), web)
            assertEquals(setOf(Person("John"), Person("Mike")), people)
            assertEquals(LocalDateTime.of(2016, 1, 1, 0, 0), creationDate)
            assertEquals(InetAddress.getByName("127.0.0.1"), host)
        }

        urlOf("classpath:data/companies.json").parseMaps().map { Company().copy(it) }.first().apply {
            val clientList = listOf(urlOf("http://c1.example.org"), urlOf("http://c2.example.org"))

            assertEquals("id", id)
            assertEquals(LocalDate.of(2014, 1, 25), foundation)
            assertEquals(LocalTime.of(11, 42), closeTime)
            assertEquals(LocalTime.of(8, 30)..LocalTime.of(14, 51), openTime)
            assertEquals(urlOf("http://example.org"), web)
            assertEquals(clientList, clients)
            assertEquals(ByteBuffer.wrap("AAEC".decodeBase64()), logo)
            assertEquals("notes", notes)
            assertEquals(setOf(Person("John"), Person("Mike")), people)
            assertEquals(setOf(DESIGN, DEVELOPMENT), departments)
            assertEquals(LocalDateTime.of(2016, 1, 1, 0, 0), creationDate)
            assertEquals(InetAddress.getByName("127.0.0.1"), host)
        }
    }
}
