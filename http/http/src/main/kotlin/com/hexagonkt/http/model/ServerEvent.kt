package com.hexagonkt.http.model

data class ServerEvent(
    val event: String? = null,
    val data: String? = null,
    val id: String? = null,
    val retry: Long? = null,
) {
    val eventData: String by lazy {
        if (event == null && data == null && id == null && retry == null)
            ":\n\n"
        else
            listOf(
                "event" to event,
                "data" to data,
                "id" to id,
                "retry" to retry,
            )
            .filter { it.second != null }
            .joinToString("\n", postfix = "\n\n") { (k, v) -> "$k: $v" }
    }
}
