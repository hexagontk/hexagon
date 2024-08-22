package com.hexagontk.rest

import com.hexagontk.core.media.*
import com.hexagontk.http.model.ContentType
import com.hexagontk.http.model.HttpBase
import com.hexagontk.serialization.*

val anyContentType = ContentType(ANY_MEDIA)
val textContentType = ContentType(TEXT_PLAIN)
val jsonContentType = ContentType(APPLICATION_JSON)
val yamlContentType = ContentType(APPLICATION_YAML)
val xmlContentType = ContentType(APPLICATION_XML)
val tomlContentType = ContentType(APPLICATION_TOML)
val csvContentType = ContentType(TEXT_CSV)

fun HttpBase.bodyList(): List<*> =
    bodyString().parseList(mediaType())

fun HttpBase.bodyMap(): Map<String, *> =
    bodyString().parseMap(mediaType())

fun HttpBase.bodyMaps(): List<Map<String, *>> =
    bodyString().parseMaps(mediaType())

fun <T> HttpBase.bodyObjects(converter: (Map<String, *>) -> T): List<T> =
    bodyMaps().map(converter)

fun <T> HttpBase.bodyObject(converter: (Map<String, *>) -> T): T =
    bodyMap().let(converter)

fun HttpBase.serializeBody(): Any =
    contentType
        ?.mediaType
        ?.let(SerializationManager::formatOfOrNull)
        ?.let { body.serialize(it) }
        ?: body

fun HttpBase.mediaType(): MediaType =
    contentType?.mediaType ?: error("Missing content type")
