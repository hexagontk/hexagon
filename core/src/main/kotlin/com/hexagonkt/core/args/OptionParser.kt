package com.hexagonkt.core.args

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

class OptionParser {

    private val allowedTypes = listOf(String::class, Boolean::class, Double::class, Int::class)

    fun <T : Arguments> parse(argumentsClass: KClass<T>, args: Array<String>): Result<T> {

        val hasUnsupportedParams = hasUnsupportedParameters(argumentsClass).getOrElse { return Result.failure(it) }

        if (hasUnsupportedParams)
            return Result.failure(IllegalArgumentException("${argumentsClass.simpleName} has unsupported parameter types"))

        val constructorParameters = argumentsClass.primaryConstructor!!.parameters
        val parameters = constructorParameters.filterNot { it.name == null }
            .fold(mutableMapOf<String, Pair<KParameter, KClass<*>>>()) { acc, kparameter ->
                acc[kparameter.name!!] = kparameter to kparameter.type.classifier as KClass<*>
                return@fold acc
            }
        val paramsNames = parameters.keys
        val argumentsClassParams = mutableMapOf<KParameter, Any>()

        var option: String? = null
        for (arg in args) {
            if (isArg(arg)) {
                val commandName = removeDashes(arg)

                option = paramsNames.find { it == commandName } ?: return Result.failure(IllegalArgumentException("bad input"))
            } else {
                if (option == null) return Result.failure(IllegalArgumentException("bad input"))
                argumentsClassParams[parameters[option]!!.first] = resolveParamValue(arg, parameters[option]!!.second)
                option = null
            }
        }

        val arguments = argumentsClass.primaryConstructor!!.callBy(argumentsClassParams)

        return Result.success(arguments)
    }

    private fun resolveParamValue(arg: String, second: KClass<*>): Any {
        return when (second.qualifiedName) {
            "kotlin.Boolean" -> arg.toBoolean()
            "kotlin.Double" -> arg.toDouble()
            "kotlin.Int" -> arg.toInt()
            "kotlin.String" -> arg
            else -> error("not supported type")
        }
    }

    private fun hasUnsupportedParameters(argumentsClass: KClass<*>): Result<Boolean> {
        val result = argumentsClass.primaryConstructor?.parameters?.any {
            val klass = it.type.classifier as? KClass<*> ?: return Result.failure(RuntimeException("not a kclass"))
            !allowedTypes.contains(klass)
        } ?: return Result.failure(RuntimeException("A proper constructor is needed"))

        return Result.success(result)
    }

    private fun removeDashes(arg: String): String = arg.replace("-", "")

    private fun isArg(arg: String) = arg.startsWith("-") || arg.startsWith("--")

}
