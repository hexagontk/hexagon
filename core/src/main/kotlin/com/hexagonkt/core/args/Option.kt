package com.hexagonkt.core.args

import com.hexagonkt.core.allowedTargetTypes
import kotlin.reflect.KClass

// TODO Use 'group' to group similar options:
//   val group: String? = null,
data class Option<T : Any>(
    val shortName: Char,
    val type: KClass<T>,
    val longName: String? = null,
    val description: String? = null,
    val optional: Boolean = true,
    val defaultValue: T? = null,
) {
    init {
        require(shortName.isLetterOrDigit()) {
            "Short name must be a letter or a digit: $shortName"
        }
        require(type in allowedTargetTypes) {
            "Type $type not in allowed types: $allowedTargetTypes"
        }
        require((longName?.trim()?.length ?: 2) > 1) {
            "Long name must be at least two characters: $longName"
        }
        require(description?.isNotBlank() ?: true) {
            "Description cannot be blank"
        }
    }
}
