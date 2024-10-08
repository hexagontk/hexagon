package com.hexagonkt.args

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CommandTest {

    @Test fun `Invalid command definitions throw errors`() {
        assertIllegalArgument { Command(" ") }
        assertIllegalArgument { Command("cmd", title = " ") }
        assertIllegalArgument { Command("cmd", description = " ") }

        assertIllegalArgument("Only the last positional parameter can be multiple") {
            Command(
                name = "cmd",
                properties = setOf(
                    Parameter<String>("first", multiple = true),
                    Parameter<Int>("second")
                )
            )
        }
    }

    @Test fun `Command creates a map of options`() {
        val option = Option<File>('s', "assets")
        val serve = Command("serve", properties = setOf(option))

        assertEquals(mapOf("s" to option, "assets" to option), serve.propertiesMap)
        assertEquals(mapOf("s" to option, "assets" to option), serve.optionsMap)
        assertEquals(emptyMap(), serve.parametersMap)

        val last = Parameter<String>("last", multiple = true)
        val cmd = Command("cmd", properties = setOf(last))

        assertEquals(mapOf("last" to last), cmd.propertiesMap)
        assertEquals(emptyMap(), cmd.optionsMap)
        assertEquals(mapOf("last" to last), cmd.parametersMap)

        val first = Parameter<String>("first")
        val scr = Command("scr", properties = setOf(first, last))

        assertEquals(mapOf("first" to first, "last" to last), scr.propertiesMap)
        assertEquals(emptyMap(), scr.optionsMap)
        assertEquals(mapOf("first" to first, "last" to last), scr.parametersMap)
    }

    @Test fun `Command can have options and parameters`() {
        val option = Option<File>('s', "assets")
        val last = Parameter<String>("last", multiple = true)
        val first = Parameter<String>("first")
        val serve = Command("serve", properties = setOf(option, first, last))

        val options = mapOf("s" to option, "assets" to option)
        val parameters = mapOf("first" to first, "last" to last)

        assertEquals(options + parameters, serve.propertiesMap)
        assertEquals(options, serve.optionsMap)
        assertEquals(parameters, serve.parametersMap)
    }

    @Test fun `Tree of commands is flattened properly`() {
        val a = Command("a",
            subcommands = setOf(
                Command("aa", subcommands = setOf(Command("aaa"), Command("aab"))),
                Command("ab", subcommands = setOf(Command("aba"), Command("abb"))),
            )
        )

        val subcommandsNames = a.subcommandsMap.keys

        assertEquals(
            setOf(
                "a aa",
                "a aa aaa",
                "a aa aab",
                "a ab",
                "a ab aba",
                "a ab abb"
            ),
            subcommandsNames
        )

        subcommandsNames.sortedByDescending { it.count { c -> c == ' ' } }
    }

    @Test fun `Find command works in a tree of commands`() {
        val aaa = Command("aaa")
        val aab = Command("aab")
        val aa = Command("aa", subcommands = setOf(aaa, aab))
        val aba = Command("aba")
        val abb = Command("abb")
        val ab = Command("ab", subcommands = setOf(aba, abb))
        val a = Command("a", subcommands = setOf(aa, ab))

        assertEquals(aa, a.findCommand(listOf("aa")))
        assertEquals(ab, a.findCommand(listOf("ab")))
        assertEquals(aaa.copy(name = "aa ${aaa.name}"), a.findCommand(listOf("aa", "aaa")))
        assertEquals(aba.copy(name = "ab ${aba.name}"), a.findCommand(listOf("ab", "aba")))
        assertEquals(aab.copy(name = "aa ${aab.name}"), a.findCommand(listOf("aa", "aab")))
        assertEquals(abb.copy(name = "ab ${abb.name}"), a.findCommand(listOf("ab", "abb")))
        assertEquals(a, a.findCommand(listOf("ax")))
    }

    @Test fun `Commands can parse their own options`() {
        Command(
            name = "cmd",
            properties = setOf(
                Flag('1', "first"),
                Option<String>('2', "second"),
                Parameter<Int>("number"),
            )
        )
        .apply { checkCases() }

        Command(
            name = "cmd",
            properties = setOf(
                Flag('1', "first"),
                Option<String>('2', "second"),
            )
        )
        .apply {
            assertIllegalState("Option 'z' not found", "-z")

            assertValues(emptyList<Any>(), "")

            assertValues(listOf(true), "-1")
            assertValues(listOf(true), "--first")
            assertValues(listOf("val"), "-2val")
            assertValues(listOf("val"), "-2 val")
            assertValues(listOf("val"), "-2=val")
            assertValues(listOf("val"), "--second val")
            assertValues(listOf("val"), "--second=val")
            assertIllegalState("No parameters", "42")
        }
    }

    @Test fun `Commands can parse multiple values properties`() {
        Command(
            name = "cmd",
            properties = setOf(
                Flag('1', "first", multiple = true),
                Option<String>('2', "second", multiple = true),
                Parameter<Int>("number", multiple = true),
            )
        )
        .apply {
            checkCases()

            assertValues(listOf(42, 43, 44), "42 43 44")
            assertValues(listOf(true, true), "-11")
            assertValues(listOf(true, true), "--first --first")
            assertValues(
                listOf("val", "more", "most", "val", "val"),
                "-2val -2 more -2=most --second val --second=val"
            )

            assertValues(listOf(true, true, "val", "val"), "-12val -12val")
            assertValues(listOf(true, true, "val", "val"), "-12 val -12 val")
            assertValues(listOf(true, true, "val", "val"), "-12=val -12=val")
            assertValues(listOf(true, true, "val", "val"), "-1 -2val -1 -2val")
            assertValues(listOf(true, true, "val", "val"), "-1 -2 val -1 -2 val")
            assertValues(listOf(true, true, "val", "val"), "-1 -2=val -1 -2=val")
            assertValues(listOf(true, true, "val", "val"), "-1 --second val -1 --second val")
            assertValues(listOf(true, true, "val", "val"), "-1 --second=val -1 --second=val")
            assertValues(listOf(true, true, "val", "val"), "--first -2 val --first -2 val")
            assertValues(listOf(true, true, "val", "val"), "--first -2=val --first -2=val")
            assertValues(
                listOf(true, true, "val", "val"),
                "--first --second=val --first --second=val"
            )
            assertValues(
                listOf(true, true, "val", "val"),
                "--first --second val --first --second val"
            )

            assertValues(listOf("val", "val", true, true), "-2val -1 -2val -1")
            assertValues(listOf("val", "val", true, true), "-2 val -1 -2 val -1")
            assertValues(listOf("val", "val", true, true), "-2=val -1 -2=val -1")
            assertValues(listOf("val", "val", true, true), "--second val -1 --second val -1")
            assertValues(listOf("val", "val", true, true), "--second=val -1 --second=val -1")
            assertValues(listOf("val", "val", true, true), "-2 val --first -2 val --first")
            assertValues(listOf("val", "val", true, true), "-2=val --first -2=val --first")
            assertValues(
                listOf("val", "val", true, true),
                "--second val --first --second val --first"
            )
            assertValues(
                listOf("val", "val", true, true),
                "--second=val --first --second=val --first"
            )

            assertValues(listOf(true, true, 42, 43), "-1 42 -1 43")
            assertValues(listOf(true, true, 42, 43), "--first 42 --first 43")
            assertValues(listOf(42, 43, true, true), "42 -1 43 -1")
            assertValues(listOf(42, 43, true, true), "42 --first 43 --first")
        }
    }

    @Test fun `Commands can parse mandatory properties`() {
        Command(
            name = "cmd",
            properties = setOf(
                Option<String>('2', "second", optional = false),
                Parameter<Int>("number", optional = false),
            )
        )
        .apply {
            assertIllegalState("Missing properties: '2'", "42")
            assertIllegalState("Missing properties: 'number'", "-2val")
        }
    }

    private fun Command.checkCases() {
        assertIllegalState("Option 'z' not found", "-z")

        assertValues(emptyList<Any>(), "")

        assertValues(listOf(true), "-1")
        assertValues(listOf(true), "--first")
        assertValues(listOf("val"), "-2val")
        assertValues(listOf("val"), "-2 val")
        assertValues(listOf("val"), "-2=val")
        assertValues(listOf("val"), "--second val")
        assertValues(listOf("val"), "--second=val")
        assertValues(listOf(42), "42")

        assertValues(listOf(true, "val"), "-12val")
        assertValues(listOf(true, "val"), "-12 val")
        assertValues(listOf(true, "val"), "-12=val")
        assertValues(listOf(true, "val"), "-1 -2val")
        assertValues(listOf(true, "val"), "-1 -2 val")
        assertValues(listOf(true, "val"), "-1 -2=val")
        assertValues(listOf(true, "val"), "-1 --second val")
        assertValues(listOf(true, "val"), "-1 --second=val")
        assertValues(listOf(true, "val"), "--first -2 val")
        assertValues(listOf(true, "val"), "--first -2=val")
        assertValues(listOf(true, "val"), "--first --second val")
        assertValues(listOf(true, "val"), "--first --second=val")
        assertValues(listOf("val", true), "-2val -1")
        assertValues(listOf("val", true), "-2 val -1")
        assertValues(listOf("val", true), "-2=val -1")
        assertValues(listOf("val", true), "--second val -1")
        assertValues(listOf("val", true), "--second=val -1")
        assertValues(listOf("val", true), "-2 val --first")
        assertValues(listOf("val", true), "-2=val --first")
        assertValues(listOf("val", true), "--second val --first")
        assertValues(listOf("val", true), "--second=val --first")

        assertValues(listOf(true, 42), "-1 42")
        assertValues(listOf(true, 42), "--first 42")
        assertValues(listOf(42, true), "42 -1")
        assertValues(listOf(42, true), "42 --first")

        assertValues(listOf("val", 42), "-2val 42")
        assertValues(listOf("val", 42), "-2 val 42")
        assertValues(listOf("val", 42), "-2=val 42")
        assertValues(listOf("val", 42), "--second val 42")
        assertValues(listOf("val", 42), "--second=val 42")

        assertValues(listOf(true, "val", 42), "-12val 42")
        assertValues(listOf(true, "val", 42), "-12 val 42")
        assertValues(listOf(true, "val", 42), "-12=val 42")
        assertValues(listOf(true, "val", 42), "-1 -2val 42")
        assertValues(listOf(true, "val", 42), "-1 -2 val 42")
        assertValues(listOf(true, "val", 42), "-1 -2=val 42")
        assertValues(listOf(true, "val", 42), "-1 --second val 42")
        assertValues(listOf(true, "val", 42), "-1 --second=val 42")
        assertValues(listOf(true, "val", 42), "--first -2 val 42")
        assertValues(listOf(true, "val", 42), "--first -2=val 42")
        assertValues(listOf(true, "val", 42), "--first --second val 42")
        assertValues(listOf(true, "val", 42), "--first --second=val 42")
        assertValues(listOf("val", true, 42), "-2val -1 42")
        assertValues(listOf("val", true, 42), "-2 val -1 42")
        assertValues(listOf("val", true, 42), "-2=val -1 42")
        assertValues(listOf("val", true, 42), "--second val -1 42")
        assertValues(listOf("val", true, 42), "--second=val -1 42")
        assertValues(listOf("val", true, 42), "-2 val --first 42")
        assertValues(listOf("val", true, 42), "-2=val --first 42")
        assertValues(listOf("val", true, 42), "--second val --first 42")
        assertValues(listOf("val", true, 42), "--second=val --first 42")

        assertValues(listOf(true, 42, "val"), "-1 42 -2val")
        assertValues(listOf(true, 42, "val"), "-1 42 -2 val")
        assertValues(listOf(true, 42, "val"), "-1 42 -2=val")
        assertValues(listOf(true, 42, "val"), "-1 42 --second val")
        assertValues(listOf(true, 42, "val"), "-1 42 --second=val")
        assertValues(listOf(true, 42, "val"), "--first 42 -2 val")
        assertValues(listOf(true, 42, "val"), "--first 42 -2=val")
        assertValues(listOf(true, 42, "val"), "--first 42 --second val")
        assertValues(listOf(true, 42, "val"), "--first 42 --second=val")
        assertValues(listOf("val", 42, true), "-2val 42 -1")
        assertValues(listOf("val", 42, true), "-2 val 42 -1")
        assertValues(listOf("val", 42, true), "-2=val 42 -1")
        assertValues(listOf("val", 42, true), "--second val 42 -1")
        assertValues(listOf("val", 42, true), "--second=val 42 -1")
        assertValues(listOf("val", 42, true), "-2 val 42 --first")
        assertValues(listOf("val", 42, true), "-2=val 42 --first")
        assertValues(listOf("val", 42, true), "--second val 42 --first")
        assertValues(listOf("val", 42, true), "--second=val 42 --first")

        assertValues(listOf(42, true, "val"), "42 -1 -2val")
        assertValues(listOf(42, true, "val"), "42 -1 -2 val")
        assertValues(listOf(42, true, "val"), "42 -1 -2=val")
        assertValues(listOf(42, true, "val"), "42 -1 --second val")
        assertValues(listOf(42, true, "val"), "42 -1 --second=val")
        assertValues(listOf(42, true, "val"), "42 --first -2 val")
        assertValues(listOf(42, true, "val"), "42 --first -2=val")
        assertValues(listOf(42, true, "val"), "42 --first --second val")
        assertValues(listOf(42, true, "val"), "42 --first --second=val")
        assertValues(listOf(42, "val", true), "42 -2val -1")
        assertValues(listOf(42, "val", true), "42 -2 val -1")
        assertValues(listOf(42, "val", true), "42 -2=val -1")
        assertValues(listOf(42, "val", true), "42 --second val -1")
        assertValues(listOf(42, "val", true), "42 --second=val -1")
        assertValues(listOf(42, "val", true), "42 -2 val --first")
        assertValues(listOf(42, "val", true), "42 -2=val --first")
        assertValues(listOf(42, "val", true), "42 --second val --first")
        assertValues(listOf(42, "val", true), "42 --second=val --first")
    }

    @Test fun `Commands throw errors with invalid arguments`() {
        Command(
            name = "cmd",
            properties = setOf(
                Flag('1', "first"),
                Option<String>('2', "second"),
                Parameter<Int>("number"),
            )
        )
        .apply {
            assertIllegalState("Option 'none' not found", "--none")
            assertIllegalState("Option 'Z' not found", "-Z")
            assertIllegalState("Unknown argument at position 2: 42", "41 42")
        }

        Command(
            name = "cmd",
            properties = setOf(
                Flag('1', "first"),
                Option<String>('2', "second"),
            )
        )
        .apply {
            assertIllegalState("Option 'none' not found", "--none")
            assertIllegalState("Option 'Z' not found", "-Z")
            assertIllegalState("No parameters", "41 42")
        }
    }

    private fun Command.assertValues(values: List<*>, args: String) {
        assertValues(values, args.split(' ').filter(String::isNotBlank))
    }

    private fun Command.assertValues(values: List<*>, args: List<String>) {
        assertEquals(values, parse(args).properties.flatMap { it.values })
    }

    private fun Command.assertIllegalState(message: String, args: List<String>) {
        assertFailsWithMessage(IllegalStateException::class, message) { parse(args) }
    }

    private fun Command.assertIllegalState(message: String, args: String) {
        assertIllegalState(message, args.split(' ').filter(String::isNotBlank))
    }
}
