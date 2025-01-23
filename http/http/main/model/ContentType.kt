package com.hexagontk.http.model

import com.hexagontk.core.media.MediaType
import com.hexagontk.core.assertEnabled
import java.nio.charset.Charset

class ContentType(
    val mediaType: MediaType,
    val boundary: String? = null,
    val charset: Charset? = null,
    val q: Double? = null,
) : HttpField {

    override val name: String = "content-type"
    override val value by lazy { text }

    override val text by lazy {
        listOfNotNull(
            mediaType.fullType,
            boundary?.let { "boundary=$it" },
            charset?.let { "charset=$it" },
            q?.let { "q=$it" }
        )
        .joinToString(";")
    }

    init {
        if (assertEnabled) {
            val a = if (boundary == null) 0 else 1
            val b = if (charset == null) 0 else 1
            val c = if (q == null) 0 else 1

            require(a + b + c in 0..1) { "Only one parameter can be set: $this" }
            require(boundary?.isNotBlank() != false) { "Boundary can not be blank" }
            require(q?.let { it in 0.0..1.0 } != false) { "Q must be in the 0 to 1 range: $q" }
        }
    }
}
