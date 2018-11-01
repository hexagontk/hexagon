package com.hexagonkt.serialization

import com.hexagonkt.helpers.Resource
import com.hexagonkt.serialization.SerializationManager.coreFormats
import com.hexagonkt.serialization.SerializationManager.defaultFormat
import com.hexagonkt.serialization.SerializationManager.formats
import com.hexagonkt.serialization.SerializationManager.formatsMap
import com.hexagonkt.serialization.SerializationManager.formatOf
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File
import java.net.URL
import kotlin.test.assertFailsWith

@Test class SerializationManagerTest {
    @BeforeMethod @AfterMethod fun resetSerializationFormats () { formats = coreFormats }

    @Test fun `User can add and remove serialization formats` () {
        assert (formats == coreFormats)
        assert (formatsMap == linkedMapOf(
            JsonFormat.contentType to JsonFormat,
            YamlFormat.contentType to YamlFormat//,
//            CsvFormat.contentType to CsvFormat
        ))

        formats = linkedSetOf(YamlFormat)
        assert (formats == linkedSetOf(YamlFormat))
        assert (formatsMap == linkedMapOf(YamlFormat.contentType to YamlFormat))

        formats = linkedSetOf(JsonFormat)
        assert (formats == linkedSetOf(JsonFormat))
        assert (formatsMap == linkedMapOf(JsonFormat.contentType to JsonFormat))

        formats = linkedSetOf(JsonFormat, YamlFormat)
        assert (formats == linkedSetOf(JsonFormat, YamlFormat))
        assert (formatsMap == linkedMapOf(
            JsonFormat.contentType to JsonFormat,
            YamlFormat.contentType to YamlFormat
        ))
    }

    @Test fun `User can change default format` () {
        assert (defaultFormat == JsonFormat)

        defaultFormat = YamlFormat
        assert (defaultFormat == YamlFormat)
    }

    @Test(expectedExceptions = [ IllegalArgumentException::class ])
    fun `User can not set an empty list of formats` () {
        formats = linkedSetOf()
    }

    @Test(expectedExceptions = [ IllegalArgumentException::class ])
    fun `User can not set a default format not loaded` () {
        formats = linkedSetOf(YamlFormat)
        defaultFormat = JsonFormat
    }

    @Test(expectedExceptions = [ IllegalStateException::class ])
    fun `Searching a format not loaded raises an exception` () {
        formats = linkedSetOf(YamlFormat)
        formatOf(JsonFormat.contentType)
    }

    fun `Searching serialization format for content types, URLs, files and resources works` () {
        assert(formatOf(JsonFormat.contentType) == JsonFormat)
        assert(formatOf(URL("http://l/a.yaml")) == YamlFormat)
        assert(formatOf(File("f.json")) == JsonFormat)
        assert(formatOf(Resource("r.yaml")) == YamlFormat)
    }

    @Test fun `MIME types return correct content type for extensions`() {
        assert(SerializationManager.contentTypeOf("json") == JsonFormat.contentType)
        assert(SerializationManager.contentTypeOf("yaml") == YamlFormat.contentType)
        assert(SerializationManager.contentTypeOf("yml") == YamlFormat.contentType)
        assert(SerializationManager.contentTypeOf("png") == "image/png")
        assert(SerializationManager.contentTypeOf("rtf") == "application/rtf")
    }

    @Test fun `MIME types return correct content type for URLs`() {
        assert(SerializationManager.contentTypeOf(URL("http://l/a.json")) == JsonFormat.contentType)
        assert(SerializationManager.contentTypeOf(URL("http://l/a.yaml")) == YamlFormat.contentType)
        assert(SerializationManager.contentTypeOf(URL("http://l/a.yml")) == YamlFormat.contentType)
        assert(SerializationManager.contentTypeOf(URL("http://l/a.png")) == "image/png")
        assert(SerializationManager.contentTypeOf(URL("http://l/a.rtf")) == "application/rtf")
    }

    @Test fun `MIME types return correct content type for files`() {
        assert(SerializationManager.contentTypeOf(File("f.json")) == JsonFormat.contentType)
        assert(SerializationManager.contentTypeOf(File("f.yaml")) == YamlFormat.contentType)
        assert(SerializationManager.contentTypeOf(File("f.yml")) == YamlFormat.contentType)
        assert(SerializationManager.contentTypeOf(File("f.png")) == "image/png")
        assert(SerializationManager.contentTypeOf(File("f.rtf")) == "application/rtf")
    }

    @Test fun `MIME types return correct content type for resources`() {
        assert(SerializationManager.contentTypeOf(Resource("r.json")) == JsonFormat.contentType)
        assert(SerializationManager.contentTypeOf(Resource("r.yaml")) == YamlFormat.contentType)
        assert(SerializationManager.contentTypeOf(Resource("r.yml")) == YamlFormat.contentType)
        assert(SerializationManager.contentTypeOf(Resource("r.png")) == "image/png")
        assert(SerializationManager.contentTypeOf(Resource("r.rtf")) == "application/rtf")
    }

    @Test fun `Not found Serialization format throws an exception`() {
        assertFailsWith<IllegalStateException> { SerializationManager.formatOf(Resource("r._")) }
        assertFailsWith<IllegalStateException> { SerializationManager.formatOf(File("r._")) }
        assertFailsWith<IllegalStateException> { SerializationManager.formatOf(URL("http://r._")) }
        assertFailsWith<IllegalStateException> { SerializationManager.formatOf("_") }
    }

    @Test fun `Not found Serialization format returns the default`() {
        assert(SerializationManager.formatOf("_", JsonFormat) == JsonFormat)
    }
}
