package com.hexagonkt.serialization

import com.hexagonkt.serialization.SerializationManager.contentTypes
import com.hexagonkt.serialization.SerializationManager.defaultFormat
import com.hexagonkt.serialization.SerializationManager.formats
import com.hexagonkt.serialization.SerializationManager.formatsMap
import com.hexagonkt.serialization.SerializationManager.getContentTypeFormat
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

@Test class SerializationManagerTest {
    @BeforeMethod @AfterMethod fun resetSerializationFormats () { formats = coreFormats }

    fun `user can add and remove serialization formats` () {
        assert (formats == coreFormats)
        assert (contentTypes == linkedSetOf(JsonFormat.contentType, YamlFormat.contentType))
        assert (formatsMap == linkedMapOf(
            JsonFormat.contentType to JsonFormat,
            YamlFormat.contentType to YamlFormat
        ))

        formats = linkedSetOf(YamlFormat)
        assert (formats == linkedSetOf(YamlFormat))
        assert (contentTypes == linkedSetOf(YamlFormat.contentType))
        assert (formatsMap == linkedMapOf(YamlFormat.contentType to YamlFormat))

        formats = linkedSetOf(JsonFormat)
        assert (formats == linkedSetOf(JsonFormat))
        assert (contentTypes == linkedSetOf(JsonFormat.contentType))
        assert (formatsMap == linkedMapOf(JsonFormat.contentType to JsonFormat))
    }

    fun `user can change default format` () {
        assert (defaultFormat == JsonFormat.contentType)

        defaultFormat = YamlFormat.contentType
        assert (defaultFormat == YamlFormat.contentType)
    }

    @Test(expectedExceptions = arrayOf(IllegalArgumentException::class))
    fun `user can not set an empty list of formats` () {
        formats = linkedSetOf()
    }

    @Test(expectedExceptions = arrayOf(IllegalArgumentException::class))
    fun `user can not set a default format not loaded` () {
        formats = linkedSetOf(YamlFormat)
        defaultFormat = JsonFormat.contentType
    }

    @Test(expectedExceptions = arrayOf(IllegalStateException::class))
    fun `searching a format not loaded raises an exception` () {
        formats = linkedSetOf(YamlFormat)
        getContentTypeFormat(JsonFormat.contentType)
    }

    fun `serialization manager can get the content type by an extension` () {
        assert(SerializationManager.getFileFormat("a.json").contentType == "application/json")
//        assert(SerializationManager.getExtensionFormat("a.yaml").contentType == "application/yaml")
    }
}
