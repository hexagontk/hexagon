package com.hexagontk.serialization.jackson

import tools.jackson.databind.*
import tools.jackson.databind.node.*

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

            is StringNode -> node.stringValue()
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
}
