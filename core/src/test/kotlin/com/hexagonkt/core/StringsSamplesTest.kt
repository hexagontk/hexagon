package com.hexagonkt.core

import kotlin.test.Test

internal class StringsSamplesTest {

    @Test fun filterVarsExample() {
        val template = "User {{user}}"
        val parameters = mapOf<Any, Any>("user" to "John")

        assert (template.filterVars(parameters) == "User John")
    }
}
