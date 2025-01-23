package com.hexagontk.serialization.test

internal class Group(
    val name: String,
    val admin: String?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Group

        if (name != other.name) return false
        if (admin != other.admin) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (admin?.hashCode() ?: 0)
        return result
    }
}
