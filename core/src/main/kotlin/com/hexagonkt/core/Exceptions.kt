package com.hexagonkt.core

import com.hexagonkt.core.text.eol

/**
 * This flag is true when assertions are enabled in the JVM (`-ea` flag). Assertions are disabled by
 * default in the JVM, but they are enabled (and should be that way) on the tests.
 */
val assertEnabled: Boolean by lazy {
    try {
        assert(false)
        false
    } catch (_: AssertionError) {
        true
    }
}

/** Syntax sugar to throw errors. */
val fail: Nothing
    get() = error("Invalid state")

/**
 * Return the stack trace array of the frames that starts with the given prefix.
 *
 * @receiver Throwable which stack trace will be filtered.
 * @param prefix Prefix used to filter stack trace elements (applied to class names).
 * @return Array with the frames of the throwable whose classes start with the given prefix.
 */
fun Throwable.filterStackTrace(prefix: String): Array<out StackTraceElement> =
    if (prefix.isEmpty())
        this.stackTrace
    else
        this.stackTrace.filter { it.className.startsWith(prefix) }.toTypedArray()

/**
 * Return this throwable as a text.
 *
 * @receiver Throwable to be printed to a string.
 * @param prefix Optional prefix to filter stack trace elements.
 * @return The filtered (if filter is provided) Throwable as a string.
 */
fun Throwable.toText(prefix: String = ""): String =
    "${this.javaClass.name}: ${this.message}" +
    this.filterStackTrace(prefix).joinToString(eol, eol) { "\tat $it" } +
    if (this.cause == null) ""
    else "${eol}Caused by: " + (this.cause as Throwable).toText(prefix)
