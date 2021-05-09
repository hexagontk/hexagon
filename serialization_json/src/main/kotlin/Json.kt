package com.hexagonkt.serialization

object Json : SerializationFormat by JacksonTextFormat(linkedSetOf("json"))
