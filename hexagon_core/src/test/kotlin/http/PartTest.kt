package com.hexagonkt.http

import com.hexagonkt.helpers.toStream
import org.testng.annotations.Test

@Test class PartTest {

    @Test fun `Full part contains expected values`() {
        val content = "content"
        val fullPart = Part(
            contentType = "text/plain",
            headers = emptyMap(),
            inputStream = content.toStream(),
            name = "name",
            size = content.length.toLong(),
            submittedFileName = "filename"
        )

        assert(fullPart.contentType == "text/plain")
        assert(fullPart.headers == emptyMap<String, List<String>>())
        assert(fullPart.inputStream.read() > 0)
        assert(fullPart.name == "name")
        assert(fullPart.size == content.length.toLong())
        assert(fullPart.submittedFileName == "filename")
    }
}
