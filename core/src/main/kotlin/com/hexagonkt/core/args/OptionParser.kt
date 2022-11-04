package com.hexagonkt.core.args

import com.hexagonkt.core.toOrNull

object OptionParser {

    private const val LONG_NAME = "--[a-z]+-?[a-z]+=?[a-zA-Z\\d.]*"
    private const val SHORT_NAME = "-[a-z]+"

    private val PARAMETER_REGEX = Regex("$LONG_NAME|$SHORT_NAME")

    fun parse(options: List<Option<*>>, args: Array<String>): Map<Option<*>, *> {

        val result = mutableMapOf<Option<*>, Any>()

        for (arg in args) {
            if (!isOption(arg)) continue

            if (!PARAMETER_REGEX.matches(arg)) error("InvalidOptionSyntaxException")

            val isLong = arg.startsWith("--")
            val optionWithoutPrefixedDashes = removePrefixedDashes(arg)

            if (isLong) {
                val split = optionWithoutPrefixedDashes.split("=")
                val option = options.find { split.first() == it.longName }
                    ?: error("InvalidOptionException")

                result[option] = when {
                    split.size > 1 -> split[1].toOrNull(option.type) ?: error("Null not allowed")
                    option.type == Boolean::class -> true
                    else -> error("OptionNeedsAValueException")
                }
            } else {
                optionWithoutPrefixedDashes.forEach { shortName ->
                    val option = options.find { it.shortName == shortName }
                        ?: error("InvalidOptionSyntaxException")
                    result[option] = true
                }
            }
        }

        return result
    }

    private fun isOption(arg: String) =
        arg.startsWith("-") || arg.startsWith("--")

    private fun removePrefixedDashes(arg: String): String {
        var result = arg
        while (result.startsWith("-")) {
            result = result.substring(1)
        }

        return result
    }
}
