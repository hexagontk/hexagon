package com.hexagonkt.serialization

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator

@Suppress("MoveLambdaOutsideParentheses") // In this case that syntax cannot be used
object YamlFormat : SerializationFormat by JacksonTextFormat(
    linkedSetOf("yaml", "yml"),
    { with(YAMLFactory()) { configure(YAMLGenerator.Feature.WRITE_DOC_START_MARKER, false) } }
)
