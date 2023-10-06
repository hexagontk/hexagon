package com.hexagonkt.serialization.jackson.json

import com.hexagonkt.core.*
import com.hexagonkt.core.text.decodeBase64
import com.hexagonkt.core.media.APPLICATION_AVRO
import com.hexagonkt.core.media.APPLICATION_PHP
import com.hexagonkt.core.text.encodeToBase64
import com.hexagonkt.serialization.Data
import com.hexagonkt.serialization.SerializationFormat
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.URL
import java.nio.ByteBuffer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter.*

internal enum class Department { DESIGN, DEVELOPMENT }

internal data class Person(val name: String) : Data<Person> {

    override val data: Map<String, *> =
        fieldsMapOf(Person::name to name)

    override fun copy(data: Map<String, *>): Person =
        copy(name = data.getOrDefault(Person::name, name))
}

internal data class Company(
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
    val creationDate: LocalDateTime = LocalDateTime.now(),
    val host: InetAddress,
    val averageMargin: Float,
) : Data<Company> {

    constructor() : this(
        id = "",
        foundation = LocalDate.now(),
        closeTime = LocalTime.now(),
        openTime = LocalTime.now()..LocalTime.now(),
        web = urlOf("http://example.com"),
        people = emptySet(),
        host = InetAddress.getLocalHost(),
        averageMargin = 0.0F,
    )

    override val data: Map<String, *> =
        fieldsMapOf(
            Company::id to id,
            Company::foundation to ISO_LOCAL_DATE.format(foundation),
            Company::closeTime to ISO_LOCAL_TIME.format(closeTime),
            Company::openTime to mapOf("start" to openTime.start.toString(), "end" to openTime.endInclusive.toString()),
            Company::web to web.toString(),
            Company::clients to clients.map(Any::toString),
            Company::logo to logo?.array()?.encodeToBase64(),
            Company::notes to notes,
            Company::people to people.map(Person::data),
            Company::departments to departments.map(Department::name),
            Company::creationDate to ISO_LOCAL_DATE_TIME.format(creationDate),
            Company::host to host.hostName,
            Company::averageMargin to averageMargin,
        )

    override fun copy(data: Map<String, *>): Company =
        copy(
            id = data.getString(Company::id) ?: id,
            foundation = data.getString(Company::foundation)?.let(LocalDate::parse) ?: foundation,
            closeTime = data.getString(Company::closeTime)?.let(LocalTime::parse) ?: closeTime,
            openTime =
                data.getMap(Company::openTime)
                    ?.let {
                        val start = (it["start"] as? String)?.let(LocalTime::parse) ?: fail
                        val end = (it["end"] as? String)?.let(LocalTime::parse) ?: fail
                        start..end
                    }
                    ?: openTime,
            web = data.getString(Company::web)?.let(::urlOf) ?: web,
            clients = data.getStrings(Company::clients)?.map(::urlOf) ?: clients,
            logo =
                data.getString(Company::logo)?.let { ByteBuffer.wrap(it.decodeBase64()) } ?: logo,
            notes = data[Company::notes],
            people =
                data.getMaps(Company::people)
                    ?.map { Person(it.requireString(Person::name)) }?.toSet() ?: people,
            departments =
                data.getStrings(Company::departments)
                    ?.map(Department::valueOf)?.toSet() ?: departments,
            creationDate =
                data.getString(Company::creationDate)?.let(LocalDateTime::parse) ?: creationDate,
            host = data.getString(Company::host)?.let { InetAddress.getByName(it) } ?: host,
            averageMargin = data.getDouble(Company::averageMargin)?.toFloat() ?: averageMargin,
        )
}

object TextTestFormat : SerializationFormat {
    override val mediaType = APPLICATION_PHP
    override val textFormat = true

    override fun serialize(instance: Any, output: OutputStream) {
        output.write(instance.toString().toByteArray())
    }

    override fun parse(input: InputStream): Any =
        listOf("text")
}

object BinaryTestFormat : SerializationFormat {
    override val mediaType = APPLICATION_AVRO
    override val textFormat = false

    override fun serialize(instance: Any, output: OutputStream) {
        output.write(instance.toString().toByteArray())
    }

    override fun parse(input: InputStream): Any =
        listOf("bytes")
}
