package com.hexagontk.http.test.examples

import com.hexagontk.core.media.APPLICATION_JSON
import com.hexagontk.http.client.HttpClientPort
import com.hexagontk.http.model.HttpRequest
import com.hexagontk.http.formatQueryString
import com.hexagontk.http.model.*
import com.hexagontk.http.server.*
import com.hexagontk.http.handlers.HttpCallbackType
import com.hexagontk.http.handlers.HttpHandler
import com.hexagontk.http.handlers.path
import com.hexagontk.http.test.BaseTest
import com.hexagontk.serialization.SerializationFormat
import com.hexagontk.serialization.SerializationManager
import org.junit.jupiter.api.*

import kotlin.test.assertEquals

@Suppress("FunctionName") // This class's functions are intended to be used only in tests
abstract class ClientMultipartTest(
    final override val clientAdapter: () -> HttpClientPort,
    final override val serverAdapter: () -> HttpServerPort,
    private val serializationFormats: List<SerializationFormat>,
    final override val serverSettings: HttpServerSettings = HttpServerSettings(),
) : BaseTest() {

    private var callback: HttpCallbackType = { this }

    override val handler: HttpHandler = path {
        post("*") { callback() }
        get("*") { callback() }
        head("*") { callback() }
        put("*") { callback() }
        delete("*") { callback() }
        trace("*") { callback() }
        options("*") { callback() }
        patch("*") { callback() }
    }

    @BeforeAll fun setUpSerializationFormats() {
        SerializationManager.formats = serializationFormats.toSet()
    }

    @BeforeEach fun resetHandler() {
        callback = {
            val contentType = ContentType(APPLICATION_JSON, charset = Charsets.UTF_8)
            val bodyString = request.bodyString()
            val bodyHeader =
                if (bodyString.endsWith("\n") || bodyString.contains("{")) "json"
                else bodyString

            ok(
                body = bodyString,
                headers = response.headers
                    + Header("body", bodyHeader)
                    + Header("ct", request.contentType?.text ?: "")
                    + Header("query-parameters", formatQueryString(queryParameters)),
                contentType = contentType,
            )
        }
    }

    @Test open fun `Form parameters are sent correctly`() {
        callback = {
            val headers = Headers(formParameters.httpFields.map { (k, v) -> Header(k, v.values) })
            ok(headers = headers)
        }

        val response = client.send(
            HttpRequest(
                formParameters = FormParameters(
                    FormParameter("p1", "v11"),
                    FormParameter("p2", "v21", "v22"),
                )
            )
        )

        val expectedHeaders = Headers(Header("p1", "v11"), Header("p2", "v21", "v22"))
        val actualHeaders =
            response.headers - "transfer-encoding" - "content-length" - "connection" - "date"

        assertEquals(expectedHeaders, actualHeaders)
    }
}
