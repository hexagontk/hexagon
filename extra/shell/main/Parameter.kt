package com.hexagontk.shell

import com.hexagontk.core.text.parseOrNull
import kotlin.reflect.KClass

class Parameter<T : Any>(
    override val type: KClass<T>,
    val name: String,
    override val description: String? = null,
    override val regex: Regex? = null,
    override val optional: Boolean = true,
    override val multiple: Boolean = false,
    override val tag: String? = null,
    override val values: List<T> = emptyList(),
) : Property<T> {

    override val names: Set<String> = setOf(name)

    companion object {
        private val parameterRegex = "[a-z0-9\\-]{2,}".toRegex()
    }

    constructor(
        type: KClass<T>,
        name: String,
        description: String? = null,
        regex: Regex? = null,
        tag: String? = null,
        value: T,
    ) : this(type, name, description, regex, true, false, tag, listOf(value))

    constructor(
        type: KClass<T>,
        name: String,
        description: String? = null,
        regex: Regex? = null,
        tag: String? = null,
        values: List<T>,
    ) : this(type, name, description, regex, true, true, tag, values)

    init {
        check("Parameter", parameterRegex)
    }

    @Suppress("UNCHECKED_CAST") // Types checked at runtime
    override fun addValues(value: Property<*>): Property<T> =
        Parameter(
            type,
            name,
            description,
            regex,
            optional,
            multiple,
            tag,
            values + value.values as List<T>
        )

    override fun addValue(value: String): Parameter<T> =
        value.parseOrNull(type)
            ?.let {
                Parameter(type, name, description, regex, optional, multiple, tag, values + it)
            }
            ?: error("Parameter '$name' of type '${typeText()}' can not hold '$value'")

    override fun clearValues(): Parameter<T> =
        Parameter(type, name, description, regex, optional, multiple, tag, emptyList())

    // TODO Only used in tests
    fun copy(
        type: KClass<T> = this.type,
        name: String = this.name,
        description: String? = this.description,
        regex: Regex? = this.regex,
        optional: Boolean = this.optional,
        multiple: Boolean = this.multiple,
        tag: String? = this.tag,
        values: List<T> = this.values,
    ): Parameter<T> =
        Parameter(type, name, description, regex, optional, multiple, tag, values)

    // TODO Only used in tests
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Parameter<*>

        if (type != other.type) return false
        if (name != other.name) return false
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
        result = 31 * result + name.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (regex?.hashCode() ?: 0)
        result = 31 * result + optional.hashCode()
        result = 31 * result + multiple.hashCode()
        result = 31 * result + (tag?.hashCode() ?: 0)
        result = 31 * result + values.hashCode()
        return result
    }
}
