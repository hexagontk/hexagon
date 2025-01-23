package com.hexagontk.serialization.jackson.json

object Json5 : JsonFormat(relaxed = true) {
    val raw = JsonFormat(false, true)
}
