package com.hexagonkt.http.server.servlet

import com.hexagonkt.http.model.ContentType
import com.hexagonkt.http.model.Header
import com.hexagonkt.http.model.HttpFields
import com.hexagonkt.http.model.HttpPartPort
import com.hexagonkt.http.parseContentType
import jakarta.servlet.http.Part

class ServletPartAdapter(private val part: Part) : HttpPartPort {

    override val body: Any by lazy {
        part.inputStream.readAllBytes()
    }

    override val headers: HttpFields<Header> by lazy {
        HttpFields(
            part.headerNames.filterNotNull().map { Header(it, part.getHeaders(it).toList()) }
        )
    }

    override val contentType: ContentType? by lazy {
        part.contentType?.let { parseContentType(it) }
    }

    override val name: String by lazy { part.name }

    override val size: Long by lazy { part.size }

    override val submittedFileName: String? by lazy { part.submittedFileName }
}
