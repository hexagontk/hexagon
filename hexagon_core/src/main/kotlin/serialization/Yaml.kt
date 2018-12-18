package com.hexagonkt.serialization

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.WRITE_DOC_START_MARKER

@Suppress("MoveLambdaOutsideParentheses") // In this case that syntax cannot be used
object Yaml: SerializationFormat by JacksonTextFormat(
    linkedSetOf("yaml", "yml"),
    { with(YAMLFactory()) { configure(WRITE_DOC_START_MARKER, false) } }
)
