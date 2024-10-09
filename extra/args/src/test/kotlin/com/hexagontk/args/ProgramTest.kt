package com.hexagontk.args

import com.hexagontk.args.Property.Companion.HELP
import com.hexagontk.args.Property.Companion.VERSION
import com.hexagontk.core.CodedException
import java.io.ByteArrayInputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ProgramTest {

    @Test fun `Invalid programs raise errors`() {
        assertIllegalArgument { Program("program", " ") }
    }

    @Test fun `Program is created properly`() {
        Program(
            name = "program",
            version = "1.0.0",
            title = "Sample program",
            description = "A simple program that does things.",
            properties = setOf(
                Flag('f', "flag"),
                Option<String>('o', "option"),
                Parameter<Int>("number")
            )
        )
    }

    @Test fun `Program input can be retrieved`() {
        val program = Program("program")
        val sin = System.`in`
        val sample = "sample input"
        System.setIn(ByteArrayInputStream(sample.toByteArray()))
        assertEquals(sample, program.input())
        assertNull(program.input())
        System.setIn(sin)
    }

    @Test fun `Program parses arguments`() {
        Program(
            name = "program",
            version = "1.0.0",
            title = "Sample program",
            description = "A simple program that does things.",
            properties = setOf(
                Flag('f', "flag"),
                Option<String>('o', "option"),
                Parameter<Int>("number")
            )
        ).apply {
            assertValues(listOf(true, "opt", 54), "-f -o opt 54")
        }
    }

    @Test fun `Program handles standard flags`() {
        val program = Program(
            name = "program",
            version = "1.0.0",
            title = "Sample program",
            description = "A simple program that does things.",
            properties = setOf(
                VERSION,
                HELP,
                Flag('f', "flag"),
                Option<String>('o', "option"),
                Parameter<Int>("number")
            )
        )

        val v = "program - Sample program (version 1.0.0)\n\nA simple program that does things."
        assertFailsWithMessage(CodedException::class, v) { program.process(listOf("-v")) }
        assertFailsWithMessage(CodedException::class, v) { program.process(listOf("--version")) }

        val h = """
            program - Sample program (version 1.0.0)

            A simple program that does things.

            USAGE
              program [-v] [-h] [-f] [-o STRING] [<number>]

            PARAMETERS
              <number>   Type: [INT]

            OPTIONS
              -o, --option STRING   Type: [STRING]

            FLAGS
              -v, --version   Show the program's version along its description
              -h, --help      Display detailed information on running this command
              -f, --flag
        """.trimIndent().trim()
        assertFailsWithMessage(CodedException::class, h) { program.process(listOf("-h")) }
        assertFailsWithMessage(CodedException::class, h) { program.process(listOf("--help")) }
    }

    @Test fun `Program handles standard flags with required properties`() {
        val program = Program(
            name = "program",
            version = "1.0.0",
            title = "Sample program",
            description = "A simple program that does things.",
            properties = setOf(
                VERSION,
                HELP,
                Flag('f', "flag"),
                Option<String>('o', "option"),
                Parameter<Int>("number", optional = false)
            )
        )

        val v = "program - Sample program (version 1.0.0)\n\nA simple program that does things."
        assertFailsWithMessage(CodedException::class, v) { program.process(listOf("-v")) }
        assertFailsWithMessage(CodedException::class, v) { program.process(listOf("--version")) }

        val h = """
            program - Sample program (version 1.0.0)

            A simple program that does things.

            USAGE
              program [-v] [-h] [-f] [-o STRING] <number>

            PARAMETERS
              <number>   Type: INT

            OPTIONS
              -o, --option STRING   Type: [STRING]

            FLAGS
              -v, --version   Show the program's version along its description
              -h, --help      Display detailed information on running this command
              -f, --flag
        """.trimIndent().trim()
        assertFailsWithMessage(CodedException::class, h) { program.process(listOf("-h")) }
        assertFailsWithMessage(CodedException::class, h) { program.process(listOf("--help")) }
    }

    @Test fun `Program handles errors`() {
        val program = Program(
            name = "program",
            version = "1.0.0",
            title = "Sample program",
            description = "A simple program that does things.",
            properties = setOf(
                Flag('f', "flag"),
                Option<String>('o', "option"),
                Parameter<Int>("number")
            )
        )

        val h = """
            USAGE
              program [-f] [-o STRING] [<number>]

            Use the --help option (-h) to get more information""".trimIndent()

        assertFailsWithMessage(CodedException::class, "Unknown argument at position 2: 2\n\n$h") {
            program.process(listOf("1", "2"))
        }
        assertFailsWithMessage(CodedException::class, "Option 'none' not found\n\n$h") {
            program.process(listOf("--none"))
        }
        program.process(listOf("1"))
        assertEquals(9, program.parse(listOf("9")).propertyValue("number"))
        assertEquals(listOf(9), program.parse(listOf("9")).propertyValues("number"))
    }

    @Test fun `Program handles subcommands`() {
        fun Program.check() {
            assertValues(listOf(true, "val"), "cmd -12val")
            assertValues(listOf(true, "val"), "cmd -12 val")
            assertValues(listOf(true, "val"), "cmd -12=val")
            assertValues(listOf(true, "val"), "cmd -1 -2val")
            assertValues(listOf(true, "val"), "cmd -1 -2 val")
            assertValues(listOf(true, "val"), "cmd -1 -2=val")
        }

        Program(
            name = "program",
            version = "1.0.0",
            title = "Sample program",
            description = "A simple program that does things.",
            properties = setOf(VERSION, HELP),
            commands = setOf(
                Command(
                    name = "cmd",
                    properties = setOf(
                        Flag('1', "first"),
                        Option<String>('2', "second"),
                    )
                )
            )
        ).apply(Program::check)

        Program(
            name = "program",
            commands = setOf(
                Command(
                    name = "cmd",
                    properties = setOf(
                        Flag('1', "first"),
                        Option<String>('2', "second"),
                    )
                )
            )
        ).apply(Program::check)
    }

    @Test fun `Program parse command line parameters`() {
        val program = Program(
            name = "program",
            version = "1.0.0",
            title = "Sample program",
            description = "A simple program that does things.",
            properties = setOf(VERSION, HELP),
            commands = setOf(
                Command(
                    name = "cmd",
                    title = "A sample subcommand",
                    description = "The subcommand description.",
                    properties = setOf(
                        HELP,
                        Flag('1', "first"),
                        Option<String>('2', "second"),
                    )
                )
            )
        )

        assert(program.parse(arrayOf("cmd", "-1")).flags.first().values.first())
        assert(program.parse(arrayOf("cmd", "--first")).flags.first().values.first())

        assertEquals(listOf(true), program.parse(listOf("cmd", "-1")).propertyValues("1"))
        assertEquals(true, program.parse(listOf("cmd", "-1")).propertyValue("1"))
        assertNull(program.parse(listOf("cmd", "-1")).propertyValueOrNull("2"))
        assertNull(program.parse(listOf("cmd", "-1")).propertyValueOrNull("second"))
        assertNull(program.parse(listOf("cmd", "--first")).propertyValueOrNull("2"))
        assertNull(program.parse(listOf("cmd", "--first")).propertyValueOrNull("second"))
        assertEquals(listOf("a"), program.parse(listOf("cmd", "-2", "a")).propertyValues("2"))
        assertEquals("a", program.parse(listOf("cmd", "-2", "a")).propertyValue("2"))

        assertEquals(true, program.parse(listOf("cmd", "--first")).propertyValue("1"))
        assertEquals("a", program.parse(listOf("cmd", "--second", "a")).propertyValue("2"))
        assertEquals(listOf(true), program.parse(listOf("cmd", "--first")).propertyValues("1"))
        assertEquals(listOf("a"), program.parse(listOf("cmd", "--second", "a")).propertyValues("2"))
        assertNull(program.parse(listOf("cmd", "-2", "a")).propertyValueOrNull("1"))
        assertNull(program.parse(listOf("cmd", "-2", "a")).propertyValueOrNull("first"))
        assertNull(program.parse(listOf("cmd", "--second", "a")).propertyValueOrNull("1"))
        assertNull(program.parse(listOf("cmd", "--second", "a")).propertyValueOrNull("first"))
    }

    @Test fun `Parsed command handles options default values`() {
        val program = Program(
            name = "program",
            version = "1.0.0",
            title = "Sample program",
            description = "A simple program that does things.",
            properties = setOf(
                Option<String>('1', "first", value = "a"),
                Option<String>('2', "second", values = listOf("b", "c")),
            )
        )

        assertEquals("a", program.parse(listOf()).propertyValue("1"))
        assertEquals("x", program.parse(listOf("-1", "x")).propertyValue("1"))
        assertEquals("x", program.parse(listOf("--first", "x")).propertyValue("1"))
        assertEquals(listOf("b", "c"), program.parse(listOf()).propertyValues("2"))
        assertEquals(listOf("x", "y"), program.parse(listOf("-2", "x", "-2", "y")).propertyValues("2"))
        assertEquals(listOf("x", "y"), program.parse(listOf("--second", "x", "--second", "y")).propertyValues("2"))
        assertEquals("a", program.parse(listOf()).propertyValue("first"))
        assertEquals("x", program.parse(listOf("-1", "x")).propertyValue("first"))
        assertEquals("x", program.parse(listOf("--first", "x")).propertyValue("first"))
        assertEquals(listOf("b", "c"), program.parse(listOf()).propertyValues("second"))
        assertEquals(listOf("x", "y"), program.parse(listOf("-2", "x", "-2", "y")).propertyValues("second"))
        assertEquals(listOf("x", "y"), program.parse(listOf("--second", "x", "--second", "y")).propertyValues("second"))
    }

    @Test fun `Parsed command handles parameters default values`() {
        val program = Program(
            name = "program",
            version = "1.0.0",
            title = "Sample program",
            description = "A simple program that does things.",
            properties = setOf(
                Parameter<Int>("numbers", values = listOf(2, 3)),
            )
        )

        assertEquals(listOf(2, 3), program.parse(listOf()).propertyValues("numbers"))
        assertEquals(listOf(4, 5), program.parse(listOf("4", "5")).propertyValues("numbers"))
    }

    @Test fun `Program describes subcommands`() {
        val program = Program(
            name = "program",
            version = "1.0.0",
            title = "Sample program",
            description = "A simple program that does things",
            properties = setOf(VERSION, HELP),
            commands = setOf(
                Command(
                    name = "cmd",
                    title = "A sample subcommand",
                    description = "The subcommand description",
                    properties = setOf(
                        HELP,
                        Flag('1', "first"),
                        Option<String>('2', "second"),
                    )
                )
            )
        )

        val v = "program - Sample program (version 1.0.0)\n\nA simple program that does things"
        assertFailsWithMessage(CodedException::class, v) { program.process(listOf("-v")) }
        assertFailsWithMessage(CodedException::class, v) { program.process(listOf("--version")) }

        val h = """
            program - Sample program (version 1.0.0)

            A simple program that does things

            USAGE
              program [-v] [-h] cmd

            COMMANDS
              cmd   A sample subcommand

            FLAGS
              -v, --version   Show the program's version along its description
              -h, --help      Display detailed information on running this command
        """.trimIndent().trim()
        assertFailsWithMessage(CodedException::class, h) { program.process(listOf("-h")) }
        assertFailsWithMessage(CodedException::class, h) { program.process(listOf("--help")) }

        val c = """
            cmd - A sample subcommand

            The subcommand description

            USAGE
              program cmd [-h] [-1] [-2 STRING]

            OPTIONS
              -2, --second STRING   Type: [STRING]

            FLAGS
              -h, --help    Display detailed information on running this command
              -1, --first
        """.trimIndent().trim()
        assertFailsWithMessage(CodedException::class, c) { program.process(listOf("cmd", "-h")) }
        assertFailsWithMessage(CodedException::class, c) { program.process(listOf("cmd", "--help")) }

        assertFailsWithMessage(CodedException::class, h) { program.process(listOf("unknown")) }
        assertFailsWithMessage(CodedException::class, h) { program.process(emptyList()) }
    }

    private fun Program.assertValues(values: List<*>, args: String) {
        assertValues(values, args.split(' ').filter(String::isNotBlank))
    }

    private fun Program.assertValues(values: List<*>, args: List<String>) {
        assertEquals(values, parse(args).properties.flatMap { it.values })
    }
}
