package com.hexagonkt.helpers

import com.hexagonkt.serialization.JsonFormat
import com.hexagonkt.serialization.YamlFormat
import org.testng.annotations.Test

@Test class MimeTest {

    @Test fun `MIME types return correct content type`() {
        assert(mimeTypes["json"] == JsonFormat.contentType)
        assert(mimeTypes["yaml"] == YamlFormat.contentType)
        assert(mimeTypes["yml"] == YamlFormat.contentType)
        assert(mimeTypes["png"] == "image/png")
        assert(mimeTypes["rtf"] == "application/rtf")
    }
}
