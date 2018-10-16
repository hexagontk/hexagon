package com.hexagonkt.serialization

import com.hexagonkt.serialization.SerializationManager.coreFormats
import com.hexagonkt.serialization.SerializationManager.defaultFormat
import com.hexagonkt.serialization.SerializationManager.formats
import com.hexagonkt.serialization.SerializationManager.formatsMap
import com.hexagonkt.serialization.SerializationManager.getContentTypeFormat
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
            YamlFormat.contentType to YamlFormat,
            CsvFormat.contentType to CsvFormat
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
        getContentTypeFormat(JsonFormat.contentType)
    }

    @Test fun `Serialization manager can get the content type by an extension` () {
        assert(SerializationManager.getFileFormat("a.json") == JsonFormat)
        assert(SerializationManager.getFileFormat("a.yaml") == YamlFormat)
        assert(SerializationManager.getFileFormat("a.yml") == YamlFormat)

        assert(SerializationManager.getFileFormat(".json") == JsonFormat)
        assert(SerializationManager.getFileFormat(".yaml") == YamlFormat)
        assert(SerializationManager.getFileFormat(".yml") == YamlFormat)
    }
}
