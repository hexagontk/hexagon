package com.hexagonkt.core.args

object OptionParser {

    private val LONG_NAME_REGEX = Regex("--[a-z]+-?[a-z]+=?[a-zA-Z\\d]*")
    private val SHORT_NAME_REGEX = Regex("-[a-z]+")
    private val SUPPORTED_TYPES = listOf(String::class, Boolean::class, Double::class, Int::class)
    private const val BOOLEAN = "kotlin.Boolean"

    fun parse(options: List<Option<*>>, args: Array<String>): Result<Map<Option<*>, *>> {

        if (hasUnsupportedParameters(options)) return Result.failure(UnsupportedArgumentTypeException)

        val result = mutableMapOf<Option<*>, Any>()

        for (arg in args) {
            if (isArg(arg)) {
                if (!hasValidSyntax(arg)) return Result.failure(InvalidOptionSyntaxException)
                val isLong = arg.startsWith("--")
                val argWithoutPrefixedDashes = removePrefixedDashes(arg)

                if (isLong) {
                    val split = argWithoutPrefixedDashes.split("=")
                    val option = options.find { split.first() == it.longName }
                        ?: return Result.failure(InvalidOptionException)

                    result[option] = if (split.size > 1) resolveParamValue(split[1], option)
                    else if (option.type.qualifiedName == BOOLEAN) {
                        true
                    } else {
                        return Result.failure(OptionNeedsAValueException)
                    }
                } else {
                    argWithoutPrefixedDashes.forEach { shortName ->
                        val option = options.find { it.shortName == shortName }
                            ?: return Result.failure(InvalidOptionSyntaxException)
                        result[option] = true
                    }
                }
            }
        }

        return Result.success(result)
    }

    private fun hasUnsupportedParameters(options: List<Option<*>>): Boolean {
        return options.any { !SUPPORTED_TYPES.contains(it.type) }
    }

    private fun isArg(arg: String) = arg.startsWith("-") || arg.startsWith("--")

    private fun hasValidSyntax(arg: String): Boolean {
        return LONG_NAME_REGEX.matches(arg) || SHORT_NAME_REGEX.matches(arg)
    }

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

