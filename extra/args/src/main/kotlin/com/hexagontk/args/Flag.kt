package com.hexagontk.args

import com.hexagontk.args.Option.Companion.optionRegex
import kotlin.reflect.KClass

data class Flag(
    override val names: Set<String>,
    override val description: String? = null,
    override val multiple: Boolean = false,
    override val tag: String? = null,
    override val values: List<Boolean> = emptyList(),
) : Property<Boolean> {

    override val optional: Boolean = true
    override val regex: Regex? = null
    override val type: KClass<Boolean> = Boolean::class

    constructor(
        shortName: Char? = null,
        name: String? = null,
        description: String? = null,
        multiple: Boolean = false,
    ) : this(setOfNotNull(shortName?.toString(), name), description, multiple)

    init {
        check("Flag", optionRegex)
    }

    @Suppress("UNCHECKED_CAST") // Types checked at runtime
    override fun addValues(value: Property<*>): Property<Boolean> =
        copy(values = values + value.values as List<Boolean>)

    override fun addValue(value: String): Flag =
        copy(values = values + true)
}
