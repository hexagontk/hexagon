package com.hexagonkt.args.formatter

import com.hexagonkt.args.Command
import com.hexagonkt.args.Formatter as ArgsFormatter
import com.hexagonkt.args.Program

class DefaultFormatter(
    private val commandFormatter: Formatter<Command> = CommandFormatter(),
    private val titleSeparator: String = "-",
    private val versionPrefix: String = "(version ",
    private val versionSuffix: String = ")",
) : ArgsFormatter {

    override fun summary(program: Program, command: Command): String {
        val description = program.command.description
        val title = listOfNotNull(
            program.command.name,
            program.command.title?.let { "$titleSeparator $it" },
            program.version?.let { "$versionPrefix${program.version}$versionSuffix" }
        )
        .joinToString(" ")

        return if (description == null) title else "$title\n\n$description"
    }

    override fun help(program: Program, command: Command): String {
        return listOf(
            definition(program, command),
            usage(program, command),
            commandFormatter.detail(command)
        ).joinToString("\n\n")
    }

    override fun error(program: Program, command: Command, exception: Exception): String {
        val helpMessage = "Use the --help option (-h) to get more information"
        return "${exception.message}\n\n${usage(program, command)}\n\n$helpMessage"
    }

    private fun definition(program: Program, command: Command): String =
        if (command == program.command) summary(program, command)
        else commandFormatter.summary(command)

    private fun usage(program: Program, command: Command): String =
        "USAGE\n  " +
            if (command == program.command) commandFormatter.definition(command)
            else program.command.name + " " + commandFormatter.definition(command)
}
