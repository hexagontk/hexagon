package com.hexagontk.shell

import com.hexagontk.core.text.parseOrNull
import kotlin.reflect.KClass

class Option<T : Any>(
    override val type: KClass<T>,
    override val names: Set<String>,
    override val description: String? = null,
    override val regex: Regex? = null,
    override val optional: Boolean = true,
    override val multiple: Boolean = false,
    override val tag: String? = null,
    override val values: List<T> = emptyList(),
) : Property<T> {

    companion object {
        val optionRegex = "([A-Za-z0-9]|[a-z0-9\\-]{2,})".toRegex()

        private fun namesOf(shortName: Char? = null, name: String? = null): Set<String> =
            setOfNotNull(shortName?.toString(), name)
    }

    constructor(
        type: KClass<T>,
        shortName: Char? = null,
        name: String? = null,
        description: String? = null,
        regex: Regex? = null,
        optional: Boolean = true,
        multiple: Boolean = false,
        tag: String? = null,
    ) : this(type, namesOf(shortName, name), description, regex, optional, multiple, tag)

    constructor(
        type: KClass<T>,
        shortName: Char? = null,
        name: String? = null,
        description: String? = null,
        regex: Regex? = null,
        tag: String? = null,
        values: List<T>,
    ) : this(type, namesOf(shortName, name), description, regex, true, true, tag, values)

    constructor(
        type: KClass<T>,
        shortName: Char? = null,
        name: String? = null,
        description: String? = null,
        regex: Regex? = null,
        tag: String? = null,
        value: T,
    ) : this(type, namesOf(shortName, name), description, regex, true, false, tag, listOf(value))

    init {
        check("Option", optionRegex)
    }

    @Suppress("UNCHECKED_CAST") // Types checked at runtime
    override fun addValues(value: Property<*>): Property<T> =
        Option(
            type,
            names,
            description,
            regex,
            optional,
            multiple,
            tag,
            values + value.values as List<T>
        )

    override fun addValue(value: String): Option<T> =
        value.parseOrNull(type)
            ?.let { Option(type, names, description, regex, optional, multiple, tag, values + it) }
            ?: error("Option '${names.first()}' of type '${typeText()}' can not hold '$value'")

    override fun clearValues(): Option<T> =
        Option(type, names, description, regex, optional, multiple, tag, emptyList())

    // TODO Only used in tests
    fun copy(
        type: KClass<T> = this.type,
        names: Set<String> = this.names,
        description: String? = this.description,
        regex: Regex? = this.regex,
        optional: Boolean = this.optional,
        multiple: Boolean = this.multiple,
        tag: String? = this.tag,
        values: List<T> = this.values,
    ): Option<T> =
        Option(type, names, description, regex, optional, multiple, tag, values)

    // TODO Only used in tests
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Option<*>

        if (type != other.type) return false
        if (names != other.names) return false
        if (description != other.description) return false
        if (regex != other.regex) return false
        if (optional != other.optional) return false
        if (multiple != other.multiple) return false
        if (tag != other.tag) return false
        if (values != other.values) return false

        return true
    }

    // TODO Only used in tests
    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + names.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (regex?.hashCode() ?: 0)
        result = 31 * result + optional.hashCode()
        result = 31 * result + multiple.hashCode()
        result = 31 * result + (tag?.hashCode() ?: 0)
        result = 31 * result + values.hashCode()
        return result
    }
}
