package com.hexagonkt.serialization

import com.hexagonkt.core.media.APPLICATION_JSON
import com.hexagonkt.core.toStream
import com.hexagonkt.core.urlOf
import com.hexagonkt.serialization.jackson.json.Json
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Path
import kotlin.test.assertFailsWith

internal class SerializationHelpersTest {

    init {
        SerializationManager.defaultFormat = Json
    }

    @Test fun `Parse URL helpers fails if parsed type does not match`() {
        assertFailsWith<IllegalStateException> {  urlOf("classpath:companies.json").parseMap() }
            .apply { assert(message?.endsWith("cannot be cast to Map") ?: false) }

        assertFailsWith<IllegalStateException> {  urlOf("classpath:company.json").parseList() }
            .apply { assert(message?.endsWith("cannot be cast to List") ?: false) }
    }

    @Test fun `Parse URL helpers generates the correct collection`() {
        assert(urlOf("classpath:companies.json").parseMaps().isNotEmpty())
        assert(urlOf("classpath:company.json").parseMap().isNotEmpty())
    }

    @Test fun `Parse file helpers generates the correct collection`() {
        val baseDir = "serialization_test/src/test/resources".let {
            if (File(it).exists()) it
            else "src/test/resources"
        }

        assert(File("$baseDir/companies.json").parseMaps().isNotEmpty())
        assert(File("$baseDir/company.json").parseMap().isNotEmpty())

        assert(Path.of("$baseDir/companies.json").parseMaps().isNotEmpty())
        assert(Path.of("$baseDir/company.json").parseMap().isNotEmpty())
    }

    @Test fun `Parse string helpers generates the correct collection`() {
        assert("""[ { "a": "b" } ]""".parseMaps().isNotEmpty())
        assert("""{ "a": "b" }""".parseMap().isNotEmpty())
        assert("""[ { "a": "b" } ]""".parseMaps(Json).isNotEmpty())
        assert("""{ "a": "b" }""".parseMap(Json).isNotEmpty())
        assert("""[ { "a": "b" } ]""".parseMaps(APPLICATION_JSON).isNotEmpty())
        assert("""{ "a": "b" }""".parseMap(APPLICATION_JSON).isNotEmpty())
    }

    @Test fun `Parse stream helpers generates the correct collection`() {
        assert("""[ { "a": "b" } ]""".toStream().parseMaps().isNotEmpty())
        assert("""{ "a": "b" }""".toStream().parseMap().isNotEmpty())
        assert("""[ { "a": "b" } ]""".toStream().parseMaps(Json).isNotEmpty())
        assert("""{ "a": "b" }""".toStream().parseMap(Json).isNotEmpty())
        assert("""[ { "a": "b" } ]""".toStream().parseMaps(APPLICATION_JSON).isNotEmpty())
        assert("""{ "a": "b" }""".toStream().parseMap(APPLICATION_JSON).isNotEmpty())
    }
}
