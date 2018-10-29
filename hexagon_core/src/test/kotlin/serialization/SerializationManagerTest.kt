package com.hexagonkt.serialization

import com.hexagonkt.serialization.SerializationManager.coreFormats
import com.hexagonkt.serialization.SerializationManager.defaultFormat
import com.hexagonkt.serialization.SerializationManager.formats
import com.hexagonkt.serialization.SerializationManager.formatsMap
import com.hexagonkt.serialization.SerializationManager.formatOf
import com.hexagonkt.serialization.SerializationManager.setFormats
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

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

        setFormats (JsonFormat, YamlFormat)
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

    @Test fun `MIME types return correct content type`() {
        assert(SerializationManager.mimeTypes["json"] == JsonFormat.contentType)
        assert(SerializationManager.mimeTypes["yaml"] == YamlFormat.contentType)
        assert(SerializationManager.mimeTypes["yml"] == YamlFormat.contentType)
        assert(SerializationManager.mimeTypes["png"] == "image/png")
        assert(SerializationManager.mimeTypes["rtf"] == "application/rtf")
    }
}
