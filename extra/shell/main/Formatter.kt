package com.hexagontk.shell

interface Formatter {
    fun summary(program: Program, command: Command): String
    fun help(program: Program, command: Command): String
    fun error(program: Program, command: Command, exception: Exception): String
}
