package com.hexagontk.args.formatter

import com.hexagontk.args.Command
import com.hexagontk.args.Option
import com.hexagontk.args.Parameter
import com.hexagontk.args.Property.Companion.HELP
import com.hexagontk.args.Property.Companion.VERSION
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CommandFormatterTest {

    private val formatter = CommandFormatter()

    @Test fun `Command summary is created properly`() {
        val cmd = Command(
            "cmd",
            "CMD Title",
            "Description of the cmd command",
            setOf(
                HELP,
                VERSION,
                Option<String>('n', "name"),
                Option<String>('o', "output"),
                Parameter<String>("source"),
                Parameter<String>("target"),
            ),
            setOf(
                Command("edit", "Edit config"),
                Command("config", "Display config"),
            ),
        )

        assertEquals("cmd - CMD Title\n\nDescription of the cmd command", formatter.summary(cmd))
        assertEquals(
            "cmd [-h] [-v] [-n STRING] [-o STRING] [<source>] [<target>] edit|config",
            formatter.definition(cmd)
        )

        val detail = """
            COMMANDS
              edit     Edit config
              config   Display config

            PARAMETERS
              <source>   Type: [STRING]
              <target>   Type: [STRING]

            OPTIONS
              -n, --name STRING     Type: [STRING]
              -o, --output STRING   Type: [STRING]

            FLAGS
              -h, --help      Display detailed information on running this command
              -v, --version   Show the program's version along its description
        """.trimIndent().trim()
        assertEquals(detail, formatter.detail(cmd))
    }
}
