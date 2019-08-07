package com.hexagonkt.serialization

import com.hexagonkt.helpers.Resource
import com.hexagonkt.serialization.SerializationManager.coreFormats
import com.hexagonkt.serialization.SerializationManager.defaultFormat
import com.hexagonkt.serialization.SerializationManager.formats
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
        assert(formats == coreFormats)
        assert(formatOf(Json.contentType) == Json)
        assert(formatOf(Yaml.contentType) == Yaml)

        formats(Yaml)
        assert(formats == linkedSetOf(Yaml))
        assert(formatOf(Json.contentType, Yaml) == Yaml)
        assert(formatOf(Yaml.contentType) == Yaml)

        formats(Json)
        assert (formats == linkedSetOf(Json))
        assert(formatOf(Json.contentType) == Json)
        assert(formatOf(Yaml.contentType, Json) == Json)

        formats(Json, Yaml)
        assert (formats == linkedSetOf(Json, Yaml))
        assert(formatOf(Json.contentType) == Json)
        assert(formatOf(Yaml.contentType) == Yaml)
    }

    @Test fun `User can change default format` () {
        assert (defaultFormat == Json)

        defaultFormat(Yaml)
        assert (defaultFormat == Yaml)
    }

    @Test(expectedExceptions = [ IllegalArgumentException::class ])
    fun `User can not set an empty list of formats` () {
        formats = linkedSetOf()
    }

    @Test(expectedExceptions = [ IllegalArgumentException::class ])
    fun `User can not set a default format not loaded` () {
        formats = linkedSetOf(Yaml)
        defaultFormat = Json
    }

    @Test(expectedExceptions = [ IllegalStateException::class ])
    fun `Searching a format not loaded raises an exception` () {
        formats = linkedSetOf(Yaml)
        formatOf(Json.contentType)
    }

    fun `Searching serialization format for content types, URLs, files and resources works` () {
        assert(formatOf(Json.contentType) == Json)
        assert(formatOf(URL("http://l/a.yaml")) == Yaml)
        assert(formatOf(File("f.json")) == Json)
        assert(formatOf(Resource("r.yaml")) == Yaml)
    }

    @Test fun `MIME types return correct content type for extensions`() {
        assert(SerializationManager.contentTypeOf("json") == Json.contentType)
        assert(SerializationManager.contentTypeOf("yaml") == Yaml.contentType)
        assert(SerializationManager.contentTypeOf("yml") == Yaml.contentType)
        assert(SerializationManager.contentTypeOf("png") == "image/png")
        assert(SerializationManager.contentTypeOf("rtf") == "application/rtf")
    }

    @Test fun `MIME types return correct content type for URLs`() {
        assert(SerializationManager.contentTypeOf(URL("http://l/a.json")) == Json.contentType)
        assert(SerializationManager.contentTypeOf(URL("http://l/a.yaml")) == Yaml.contentType)
        assert(SerializationManager.contentTypeOf(URL("http://l/a.yml")) == Yaml.contentType)
        assert(SerializationManager.contentTypeOf(URL("http://l/a.png")) == "image/png")
        assert(SerializationManager.contentTypeOf(URL("http://l/a.rtf")) == "application/rtf")

        assert(formatOf(URL("http://l/a.json")) == Json)
        assert(formatOf(URL("http://l/a.yaml")) == Yaml)
        assert(formatOf(URL("http://l/a.yml")) == Yaml)
    }

    @Test fun `MIME types return correct content type for files`() {
        assert(SerializationManager.contentTypeOf(File("f.json")) == Json.contentType)
        assert(SerializationManager.contentTypeOf(File("f.yaml")) == Yaml.contentType)
        assert(SerializationManager.contentTypeOf(File("f.yml")) == Yaml.contentType)
        assert(SerializationManager.contentTypeOf(File("f.png")) == "image/png")
        assert(SerializationManager.contentTypeOf(File("f.rtf")) == "application/rtf")

        assert(formatOf(File("f.json")) == Json)
        assert(formatOf(File("f.yaml")) == Yaml)
        assert(formatOf(File("f.yml")) == Yaml)
    }

    @Test fun `MIME types return correct content type for resources`() {
        assert(SerializationManager.contentTypeOf(Resource("r.json")) == Json.contentType)
        assert(SerializationManager.contentTypeOf(Resource("r.yaml")) == Yaml.contentType)
        assert(SerializationManager.contentTypeOf(Resource("r.yml")) == Yaml.contentType)
        assert(SerializationManager.contentTypeOf(Resource("r.png")) == "image/png")
        assert(SerializationManager.contentTypeOf(Resource("r.rtf")) == "application/rtf")

        assert(formatOf(Resource("r.json")) == Json)
        assert(formatOf(Resource("r.yaml")) == Yaml)
        assert(formatOf(Resource("r.yml")) == Yaml)
    }

    @Test fun `Not found Serialization format throws an exception`() {
        assertFailsWith<IllegalStateException> { formatOf(Resource("r._")) }
        assertFailsWith<IllegalStateException> { formatOf(File("r._")) }
        assertFailsWith<IllegalStateException> { formatOf(URL("http://r._")) }
        assertFailsWith<IllegalStateException> { formatOf("_") }
    }

    @Test fun `Not found Serialization format returns the default`() {
        assert(formatOf("_", Json) == Json)
    }
}
