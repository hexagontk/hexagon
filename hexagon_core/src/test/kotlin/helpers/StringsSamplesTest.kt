package com.hexagonkt.helpers

import org.junit.jupiter.api.Test

internal class StringsSamplesTest {

    @Test fun filterVarsExample() {
        val template = "User {{user}}"
        val parameters = mapOf<Any, Any>("user" to "John")

        assert (template.filterVars(parameters) == "User John")
        assert (template.filterVars() == template)
    }

    @Test fun filterVarsVarargExample() {
        val template = "User {{user}}"
        val parameters = Pair("user", "John")

        assert (template.filterVars(parameters) == "User John")
        assert (template.filterVars() == template)
    }
}
