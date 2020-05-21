package com.hexagonkt.http.server.test

data class TestSession(
    var attributes: Map<String, Any?> = emptyMap(),
    var creationTime: Long? = null,
    var id: String? = null,
    var maxInactiveInterval: Int? = null,
    var lastAccessedTime: Long? = null
)
