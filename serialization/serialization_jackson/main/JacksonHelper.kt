package com.hexagontk.serialization.jackson

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.json.JsonReadFeature.*
import com.fasterxml.jackson.core.json.JsonWriteFeature.QUOTE_FIELD_NAMES
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.MapperFeature.SORT_PROPERTIES_ALPHABETICALLY
import com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.node.*

object JacksonHelper {

    fun mapNode(node: JsonNode): Any =
        nodeToCollection(node) ?: error("Parsed content is 'null'")

    internal fun nodeToCollection(node: JsonNode): Any? =
        when (node) {

            is ArrayNode -> node.toList().map { nodeToCollection(it) }
            is ObjectNode -> {
                var map = emptyMap<String, Any?>()

                for (f in node.properties())
                    map = map + (f.key to nodeToCollection(f.value))

                map
            }

            is TextNode -> node.textValue()
            is BigIntegerNode -> node.bigIntegerValue()
            is BooleanNode -> node.booleanValue()
            is DecimalNode -> node.doubleValue()
            is DoubleNode -> node.doubleValue()
            is FloatNode -> node.floatValue()
            is IntNode -> node.intValue()
            is LongNode -> node.longValue()
            is NullNode -> null
            is BinaryNode -> node.binaryValue()

            else -> error("Unknown node type: ${node::class.qualifiedName}")
        }

    internal fun createMapper(mapperFactory: JsonFactory): JsonMapper =
        baseMapper(mapperFactory).build()

    internal fun createRelaxedMapper(mapperFactory: JsonFactory): JsonMapper =
        baseMapper(mapperFactory)
            .configure(ALLOW_UNQUOTED_FIELD_NAMES, true)
            .configure(ALLOW_JAVA_COMMENTS, true)
            .configure(ALLOW_SINGLE_QUOTES, true)
            .configure(ALLOW_TRAILING_COMMA, true)
            .configure(ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true)
            .configure(ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS, true)
            .configure(ALLOW_UNESCAPED_CONTROL_CHARS, true)
            .configure(QUOTE_FIELD_NAMES, false)
            .build()

    private fun baseMapper(mapperFactory: JsonFactory): JsonMapper.Builder =
        JsonMapper.builder(mapperFactory)
            .findAndAddModules()
            .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(FAIL_ON_EMPTY_BEANS, false)
            .configure(FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
            .configure(ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .configure(SORT_PROPERTIES_ALPHABETICALLY, false)
}
