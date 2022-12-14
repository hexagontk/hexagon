package com.hexagonkt.serialization.jackson.yaml

object Yaml : YamlFormat() {
    val raw = YamlFormat(false)
}
