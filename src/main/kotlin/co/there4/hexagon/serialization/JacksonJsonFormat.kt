package co.there4.hexagon.serialization

import co.there4.hexagon.serialization.JacksonSerializer.mapper
import com.fasterxml.jackson.core.util.DefaultIndenter.SYSTEM_LINEFEED_INSTANCE

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.*
import kotlin.reflect.KClass

class JacksonJsonFormat : SerializationFormat {
    override val contentType = "application/json"

    private val writer = createObjectWriter ()

    fun createObjectWriter (): ObjectWriter {
        val printer = DefaultPrettyPrinter ().withArrayIndenter (SYSTEM_LINEFEED_INSTANCE)
        return mapper.writer (printer)
    }

    override fun serialize(obj: Any) = writer.writeValueAsString (obj)

    override fun <T : Any> parse(text: String, type: KClass<T>) =
        mapper.readValue (text, type.java)

    override fun <T : Any> parseList(text: String, type: KClass<T>): List<T> {
        val listType = mapper.getTypeFactory().constructCollectionType(List::class.java, type.java)
        return mapper.readValue (text, listType)
    }
}
