package com.hexagonkt.serialization.jackson.yaml

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.WRITE_DOC_START_MARKER
import com.hexagonkt.core.media.ApplicationMedia
import com.hexagonkt.core.media.MediaType
import com.hexagonkt.serialization.jackson.JacksonTextFormat

// TODO Convert to class to allow passing configuration options
@Suppress("MoveLambdaOutsideParentheses") // In this case that syntax cannot be used
object Yaml : JacksonTextFormat(
    { with(YAMLFactory()) { configure(WRITE_DOC_START_MARKER, false) } }
) {

    override val mediaType: MediaType = ApplicationMedia.YAML
}
