package com.hexagontk.serialization.test

import java.time.LocalDate

internal class Person(
    val givenName: String,
    val familyName: String,
    val birthDate: LocalDate,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Person

        if (givenName != other.givenName) return false
        if (familyName != other.familyName) return false
        if (birthDate != other.birthDate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = givenName.hashCode()
        result = 31 * result + familyName.hashCode()
        result = 31 * result + birthDate.hashCode()
        return result
    }
}
