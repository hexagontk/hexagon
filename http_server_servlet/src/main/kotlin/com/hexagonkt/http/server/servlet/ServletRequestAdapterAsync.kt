package com.hexagonkt.http.server.servlet

import com.hexagonkt.core.MultiMap
import com.hexagonkt.core.multiMapOf
import com.hexagonkt.http.model.*
import com.hexagonkt.http.parseContentType
import jakarta.servlet.http.HttpServletRequest
import java.lang.StringBuilder

internal class ServletRequestAdapterAsync(
    req : HttpServletRequest,
    override val body: ByteArray,
) : ServletRequestAdapter(req) {

    private val partNameRe: Regex = ".*name=\"(.*?)\".*".toRegex()
    private val partFileNameRe: Regex = ".*name=\"(.*?)\".*".toRegex()

    // TODO Parts parsing is a hack, can be improved (a lot)
    override val parts: List<HttpPartPort> by lazy {
        var partList: List<HttpPart> = emptyList()
        var partHeaders: MultiMap<String, String> = multiMapOf()
        val partBody = StringBuilder()

        val boundary = contentType?.boundary
            ?: error("Part fetching requires content type boundary")

        var inBody = false
        val bodyString = String(body)
        val bodyLines = bodyString.lines()

        bodyLines.forEach { ln ->
            when {
                ln.contains(boundary) -> {
                    if (partHeaders.isNotEmpty() || partBody.isNotBlank()) {
                        val bodyBytes = partBody.toString().removeSuffix("\n").toByteArray()
                        val type = partHeaders["content-type"]?.let { ct -> parseContentType(ct) }
                        val size = bodyBytes.size.toLong()
                        val disposition = partHeaders["content-disposition"]
                        val name = disposition
                            ?.let { partNameRe.matchEntire(it)?.groupValues?.get(1) ?: "" }
                            ?: ""
                        val fileName = disposition
                            ?.let { partFileNameRe.matchEntire(it)?.groupValues?.get(1) }

                        partList = partList + HttpPart(
                            name = name,
                            body = bodyBytes,
                            headers = partHeaders,
                            contentType = type,
                            size = size,
                            submittedFileName = fileName,
                        )

                        partHeaders = multiMapOf()
                        partBody.clear()
                        inBody = false
                    }
                }

                !inBody && ln.isNotBlank() ->
                    partHeaders += ln
                        .split(":")
                        .map { it.trim() }
                        .let { it.first().lowercase() to it.last() }

                !inBody && ln.isBlank() ->
                    inBody = true

                inBody ->
                    partBody.append(ln).append("\n")
            }
        }

        partList
    }

    override val formParameters: HttpFields<HttpFormParameter> by lazy {
        val parameters = parts
            .map { it.name to it.bodyString() }
            .groupBy({ it.first }, { it.second })
            .map { (k, v) -> HttpFormParameter(k, v) }

        HttpFields(parameters)
    }
}
