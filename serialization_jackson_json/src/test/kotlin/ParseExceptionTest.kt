package com.hexagonkt.serialization.json

import com.fasterxml.jackson.databind.JsonMappingException
import com.hexagonkt.serialization.json.JacksonHelper.parseException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

internal class ParseExceptionTest {

    @Test fun `parseException obtains field name properly`() {
        assert(parseException(null).field == "")
        assert(parseException(IllegalStateException()).field == "")
        assert(parseException(JsonMappingException(null, "message")).field == "")

        val jsonMappingException = JsonMappingException(null, "message")
        jsonMappingException.prependPath("type", "field")
        assert(parseException(jsonMappingException).field == "java.lang.String[\"field\"]")

        val mockedJsonMappingException = mockk<JsonMappingException>()
        every { mockedJsonMappingException.pathReference } returns null
        assert(parseException(mockedJsonMappingException).field == "")
    }
}
