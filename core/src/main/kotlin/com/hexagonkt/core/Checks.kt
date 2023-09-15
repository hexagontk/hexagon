package com.hexagonkt.core

import java.time.LocalDate
import kotlin.reflect.KProperty1

fun <T : Any> T.requireNotBlank(field: KProperty1<T, CharSequence?>) {
    val fieldValue = field.get(this)
    require(fieldValue?.isNotBlank() ?: true) { "'${field.name}' cannot be blank" }
}

fun <T : Any> T.requireNotBlanks(field: KProperty1<T, Collection<CharSequence>?>) {
    val fieldValue = field.get(this) ?: emptyList()
    require(fieldValue.all { it.isNotBlank() }) { "'${field.name}' cannot contain blanks" }
}

fun <T : Any> T.requireBefore(field: KProperty1<T, LocalDate?>, date: LocalDate = LocalDate.now()) {
    val fieldValue = field.get(this)
    require(fieldValue?.isBefore(date) ?: true) {
        "'${field.name}' must be before $date: $fieldValue"
    }
}

fun <T : Any> T.requireBeforeOrEquals(
    field: KProperty1<T, LocalDate?>,
    date: LocalDate = LocalDate.now()
) {
    requireBefore(field, date.plusDays(1))
}

fun <T : Any, N : Number> T.requireGreater(field: KProperty1<T, Comparable<N>?>, min: N) {
    val fieldValue = field.get(this)
    require((fieldValue?.compareTo(min) ?: 1) > 0) {
        "'${field.name}' must be greater than $min: $fieldValue"
    }
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
