package com.hexagonkt.serialization.yaml

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.WRITE_DOC_START_MARKER
import com.hexagonkt.serialization.json.JacksonTextFormat

@Suppress("MoveLambdaOutsideParentheses") // In this case that syntax cannot be used
object Yaml : JacksonTextFormat(
    linkedSetOf("yaml", "yml"),
    { with(YAMLFactory()) { configure(WRITE_DOC_START_MARKER, false) } }
)
