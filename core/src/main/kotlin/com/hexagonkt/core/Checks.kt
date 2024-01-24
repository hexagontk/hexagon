package com.hexagonkt.core

import kotlin.reflect.KProperty1

fun <T : Any> T.requireNotBlank(field: KProperty1<T, CharSequence?>) {
    val fieldValue = field.get(this)
    require(fieldValue?.isNotBlank() ?: true) { "'${field.name}' cannot be blank" }
}

fun <T : Any> T.requireNotBlanks(field: KProperty1<T, Collection<CharSequence>?>) {
    val fieldValue = field.get(this) ?: emptyList()
    require(fieldValue.all { it.isNotBlank() }) { "'${field.name}' cannot contain blanks" }
}

fun <T : Any, N> T.requireGreater(field: KProperty1<T, Comparable<N>?>, min: N) {
    val fieldValue = field.get(this)
    require((fieldValue?.compareTo(min) ?: 1) > 0) {
        "'${field.name}' must be greater than $min: $fieldValue"
    }
}

fun <T : Any, N> T.requireGreaterOrEquals(field: KProperty1<T, Comparable<N>?>, min: N) {
    val fieldValue = field.get(this)
    require((fieldValue?.compareTo(min) ?: 0) >= 0) {
        "'${field.name}' must be equals or greater than $min: $fieldValue"
    }
}

fun <T : Any, N> T.requireLower(field: KProperty1<T, Comparable<N>?>, min: N) {
    val fieldValue = field.get(this)
    require((fieldValue?.compareTo(min) ?: -1) < 0) {
        "'${field.name}' must be lower than $min: $fieldValue"
    }
}

fun <T : Any, N> T.requireLowerOrEquals(field: KProperty1<T, Comparable<N>?>, min: N) {
    val fieldValue = field.get(this)
    require((fieldValue?.compareTo(min) ?: 0) <= 0) {
        "'${field.name}' must be equals or lower than $min: $fieldValue"
    }
}

fun <T : Any, N> T.requireGreater(field: KProperty1<T, Comparable<N>?>, field2: KProperty1<T, N?>) {
    field2.get(this)?.let { requireGreater(field, it) }
}

fun <T : Any, N> T.requireGreaterOrEquals(
    field: KProperty1<T, Comparable<N>?>,
    field2: KProperty1<T, N?>
) {
    field2.get(this)?.let { requireGreaterOrEquals(field, it) }
}

fun <T : Any, N> T.requireLower(field: KProperty1<T, Comparable<N>?>, field2: KProperty1<T, N?>) {
    field2.get(this)?.let { requireLower(field, it) }
}

fun <T : Any, N> T.requireLowerOrEquals(
    field: KProperty1<T, Comparable<N>?>,
    field2: KProperty1<T, N?>
) {
    field2.get(this)?.let { requireLowerOrEquals(field, it) }
}

/**
 * Ensure a collection has a fixed number of elements.
 *
 * @receiver Collection which size will be checked.
 * @param count Required number of elements.
 * @return Receiver reference (to allow call chaining).
 */
fun <Z> Collection<Z>.checkSize(count: IntRange): Collection<Z> = this.apply {
    check(size in count) { "$size items while expecting only $count element" }
}

/**
 * Execute a list of code block collecting the exceptions they may throw, in case there is any
 * error, it throws a [MultipleException] with all the thrown exceptions.
 *
 * @param message Error message.
 * @param blocks Blocks of code executed and checked.
 */
fun check(message: String, vararg blocks: () -> Unit) {
    val exceptions: List<Exception> = blocks.mapNotNull {
        try {
            it()
            null
        }
        catch(e: Exception) {
            e
        }
    }

    if (exceptions.isNotEmpty())
        throw MultipleException(message, exceptions)
}
