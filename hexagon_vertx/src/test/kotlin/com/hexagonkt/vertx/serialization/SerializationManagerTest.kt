package com.hexagonkt.vertx.serialization

import com.hexagonkt.serialization.JsonFormat
import com.hexagonkt.serialization.SerializationManager.coreFormats
import com.hexagonkt.serialization.YamlFormat
import com.hexagonkt.vertx.serialization.SerializationManager.defaultFormat
import com.hexagonkt.vertx.serialization.SerializationManager.formats
import com.hexagonkt.vertx.serialization.SerializationManager.formatsMap
import com.hexagonkt.vertx.serialization.SerializationManager.getContentTypeFormat
import com.hexagonkt.vertx.serialization.SerializationManager.setFormats
import org.junit.After
import org.junit.Before
import org.junit.Test

class SerializationManagerTest {
    @Before @After fun resetSerializationFormats () { formats = coreFormats }

    @Test fun `user can add and remove serialization formats` () {
        assert (formats == coreFormats)
        assert (formatsMap == linkedMapOf(
            JsonFormat.contentType to JsonFormat,
            YamlFormat.contentType to YamlFormat
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

    @Test fun `user can change default format` () {
        assert (defaultFormat == JsonFormat)

        defaultFormat = YamlFormat
        assert (defaultFormat == YamlFormat)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `user can not set an empty list of formats` () {
        formats = linkedSetOf()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `user can not set a default format not loaded` () {
        formats = linkedSetOf(YamlFormat)
        defaultFormat = JsonFormat
    }

    @Test(expected = IllegalStateException::class)
    fun `searching a format not loaded raises an exception` () {
        formats = linkedSetOf(YamlFormat)
        getContentTypeFormat(JsonFormat.contentType)
    }

    @Test fun `serialization manager can get the content type by an extension` () {
        assert(SerializationManager.getFileFormat("a.json") == JsonFormat)
        assert(SerializationManager.getFileFormat("a.yaml") == YamlFormat)
        assert(SerializationManager.getFileFormat("a.yml") == YamlFormat)

        assert(SerializationManager.getFileFormat(".json") == JsonFormat)
        assert(SerializationManager.getFileFormat(".yaml") == YamlFormat)
        assert(SerializationManager.getFileFormat(".yml") == YamlFormat)
    }
}
