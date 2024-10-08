package com.hexagonkt.args

inline fun <reified T : Any> Option(
    shortName: Char? = null,
    name: String? = null,
    description: String? = null,
    regex: Regex? = null,
    optional: Boolean = true,
    multiple: Boolean = false,
    tag: String? = null,
) : Option<T> =
    Option(T::class, shortName, name, description, regex, optional, multiple, tag)

inline fun <reified T : Any> Option(
    shortName: Char? = null,
    name: String? = null,
    description: String? = null,
    regex: Regex? = null,
    tag: String? = null,
    values: List<T>
) : Option<T> =
    Option(T::class, shortName, name, description, regex, tag, values)

inline fun <reified T : Any> Option(
    shortName: Char? = null,
    name: String? = null,
    description: String? = null,
    regex: Regex? = null,
    tag: String? = null,
    value: T
) : Option<T> =
    Option(T::class, shortName, name, description, regex, tag, value)

inline fun <reified T : Any> Parameter(
    name: String,
    description: String? = null,
    regex: Regex? = null,
    optional: Boolean = true,
    multiple: Boolean = false,
    tag: String? = null,
) : Parameter<T> =
    Parameter(T::class, name, description, regex, optional, multiple, tag)

inline fun <reified T : Any> Parameter(
    name: String,
    description: String? = null,
    regex: Regex? = null,
    tag: String? = null,
    values: List<T>,
) : Parameter<T> =
    Parameter(T::class, name, description, regex, tag, values)

inline fun <reified T : Any> Parameter(
    name: String,
    description: String? = null,
    regex: Regex? = null,
    tag: String? = null,
    value: T,
) : Parameter<T> =
    Parameter(T::class, name, description, regex, tag, value)
