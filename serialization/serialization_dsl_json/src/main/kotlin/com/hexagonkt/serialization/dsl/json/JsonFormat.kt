package com.hexagonkt.serialization.dsl.json

import com.dslplatform.json.DslJson
import com.dslplatform.json.PrettifyOutputStream
import com.dslplatform.json.runtime.Settings
import com.hexagonkt.core.media.APPLICATION_JSON
import com.hexagonkt.core.media.MediaType
import com.hexagonkt.serialization.SerializationFormat
import java.io.InputStream
import java.io.OutputStream

open class JsonFormat(private val prettyPrint: Boolean = true) : SerializationFormat {
    override val textFormat: Boolean = true
    override val mediaType: MediaType = APPLICATION_JSON

    private val dslJson: DslJson<Any> =
        DslJson(Settings.withRuntime())

    override fun serialize(instance: Any, output: OutputStream) {
        val stream = if (prettyPrint) PrettifyOutputStream(output) else output
        dslJson.serialize(instance, stream)
    }

    override fun parse(input: InputStream): Any =
        dslJson.deserialize(Any::class.java, input) ?: error("Error parsing input")
}
