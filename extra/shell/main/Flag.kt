package com.hexagontk.shell

import com.hexagontk.shell.Option.Companion.optionRegex
import kotlin.reflect.KClass

class Flag(
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
        Flag(names, description, multiple, tag, values + value.values as List<Boolean>)

    override fun addValue(value: String): Flag =
        Flag(names, description, multiple, tag, values + true)

    override fun clearValues(): Flag =
        Flag(names, description, multiple, tag, emptyList())

    // TODO Only used in tests
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Flag

        if (names != other.names) return false
        if (description != other.description) return false
        if (multiple != other.multiple) return false
        if (tag != other.tag) return false
        if (values != other.values) return false

        return true
    }

    // TODO Only used in tests
    override fun hashCode(): Int {
        var result = names.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + multiple.hashCode()
        result = 31 * result + (tag?.hashCode() ?: 0)
        result = 31 * result + values.hashCode()
        return result
    }
}
