package com.hexagonkt.serialization.jackson

import com.fasterxml.jackson.databind.node.*
import com.hexagonkt.serialization.jackson.JacksonHelper.mapNode
import com.hexagonkt.serialization.jackson.JacksonHelper.nodeToCollection
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigInteger
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class JacksonHelperTest {

    @Test fun `All node types are correctly converted to JVM types`() {
        assertEquals("text", mapNode(TextNode("text")))
        assertEquals(BigInteger.TEN, mapNode(BigIntegerNode(BigInteger.TEN)))
        assertEquals(false, mapNode(BooleanNode.FALSE))
        assertEquals(0.1, mapNode(DoubleNode(0.1)))
        assertEquals(0.5F, mapNode(FloatNode(0.5F)))
        assertEquals(10, mapNode(IntNode(10)))
        assertEquals(100L, mapNode(LongNode(100L)))
        assertEquals(null, nodeToCollection(NullNode.instance))
        assertContentEquals(
            "bytes".toByteArray(),
            mapNode(BinaryNode("bytes".toByteArray())) as ByteArray
        )
    }
}
