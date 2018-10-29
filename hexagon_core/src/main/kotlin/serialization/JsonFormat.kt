package com.hexagonkt.serialization

object JsonFormat : SerializationFormat by JacksonTextFormat(linkedSetOf("json"))
