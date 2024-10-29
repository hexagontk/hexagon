package com.hexagontk.args

interface Formatter {
    fun summary(program: Program, command: Command): String
    fun help(program: Program, command: Command): String
    fun error(program: Program, command: Command, exception: Exception): String
}
