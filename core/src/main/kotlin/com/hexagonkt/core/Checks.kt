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
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @receiver .
 * @param count .
 * @return .
 */
fun <Z> Collection<Z>.checkSize(count: IntRange): Collection<Z> = this.apply {
    check(size in count) { "$size items while expecting only $count element" }
}
