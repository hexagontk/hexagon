package com.hexagonkt.serialization

import com.hexagonkt.core.media.ApplicationMedia.JSON
import com.hexagonkt.core.toStream
import com.hexagonkt.serialization.jackson.json.Json
import org.junit.jupiter.api.Test
import java.io.File
import java.net.URL
import kotlin.test.assertFailsWith

internal class SerializationHelpersTest {

    init {
        SerializationManager.defaultFormat = Json
    }

    @Test fun `Parse URL helpers fails if parsed type does not match`() {
        assertFailsWith<IllegalStateException> {  URL("classpath:companies.json").parseMap() }
            .apply { assert(message?.endsWith("cannot be cast to Map") ?: false) }

        assertFailsWith<IllegalStateException> {  URL("classpath:company.json").parseList() }
            .apply { assert(message?.endsWith("cannot be cast to List") ?: false) }
    }

    @Test fun `Parse URL helpers generates the correct collection`() {
        assert(URL("classpath:companies.json").parseList().isNotEmpty())
        assert(URL("classpath:company.json").parseMap().isNotEmpty())
    }

    @Test fun `Parse file helpers generates the correct collection`() {
        val baseDir = "serialization_test/src/test/resources".let {
            if (File(it).exists()) it
            else "src/test/resources"
        }

        assert(File("$baseDir/companies.json").parseList().isNotEmpty())
        assert(File("$baseDir/company.json").parseMap().isNotEmpty())
    }

    @Test fun `Parse string helpers generates the correct collection`() {
        assert("""[ { "a": "b" } ]""".parseList().isNotEmpty())
        assert("""{ "a": "b" }""".parseMap().isNotEmpty())
        assert("""[ { "a": "b" } ]""".parseList(Json).isNotEmpty())
        assert("""{ "a": "b" }""".parseMap(Json).isNotEmpty())
        assert("""[ { "a": "b" } ]""".parseList(JSON).isNotEmpty())
        assert("""{ "a": "b" }""".parseMap(JSON).isNotEmpty())
    }

    @Test fun `Parse stream helpers generates the correct collection`() {
        assert("""[ { "a": "b" } ]""".toStream().parseList().isNotEmpty())
        assert("""{ "a": "b" }""".toStream().parseMap().isNotEmpty())
        assert("""[ { "a": "b" } ]""".toStream().parseList(Json).isNotEmpty())
        assert("""{ "a": "b" }""".toStream().parseMap(Json).isNotEmpty())
        assert("""[ { "a": "b" } ]""".toStream().parseList(JSON).isNotEmpty())
        assert("""{ "a": "b" }""".toStream().parseMap(JSON).isNotEmpty())
    }
}
