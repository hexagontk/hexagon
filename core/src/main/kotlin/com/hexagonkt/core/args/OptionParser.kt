package com.hexagonkt.core.args

object OptionParser {

    private const val LONG_NAME = "--[a-z]+-?[a-z]+=?[a-zA-Z\\d.]*"
    private const val SHORT_NAME = "-[a-z]+"

    private val PARAMETER_REGEX = Regex("$LONG_NAME|$SHORT_NAME")
    private val SUPPORTED_TYPES = listOf(String::class, Boolean::class, Double::class, Int::class)
    private const val BOOLEAN = "kotlin.Boolean"

    fun parse(options: List<Option<*>>, args: Array<String>): Map<Option<*>, *> {

        check(!hasUnsupportedParameters(options)) { "UnsupportedArgumentTypeException" }

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
                    split.size > 1 -> resolveParamValue(split[1], option)
                    option.type.qualifiedName == BOOLEAN -> true
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

    private fun hasUnsupportedParameters(options: List<Option<*>>): Boolean {
        return options.any { !SUPPORTED_TYPES.contains(it.type) }
    }

    private fun isOption(arg: String) = arg.startsWith("-") || arg.startsWith("--")

    private fun removePrefixedDashes(arg: String): String {
        var result = arg
        while (result.startsWith("-")) {
            result = result.substring(1)
        }

        return result
    }

    private fun resolveParamValue(arg: String, option: Option<*>): Any {
        return when (option.type.qualifiedName) {
            BOOLEAN -> arg.toBoolean()
            "kotlin.Double" -> arg.toDouble()
            "kotlin.Int" -> arg.toInt()
            "kotlin.String" -> arg
            else -> error("not supported type")
        }
    }
}
