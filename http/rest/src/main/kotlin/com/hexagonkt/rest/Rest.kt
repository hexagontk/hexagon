package com.hexagonkt.rest

import com.hexagonkt.http.model.HttpBase
import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.serialization.parseList
import com.hexagonkt.serialization.parseMap

fun HttpBase.bodyList(): List<*> =
    bodyString().parseList(mediaType())

fun HttpBase.bodyMap(): Map<*, *> =
    bodyString().parseMap(mediaType())

fun HttpBase.mediaType() =
    contentType?.mediaType ?: SerializationManager.requireDefaultFormat().mediaType
