package com.hexagonkt.core

import java.util.*
import kotlin.reflect.KClass

/** Set of allowed country codes in this JVM. */
val countryCodes: Set<String> by lazy {
    Locale.getISOCountries().toSet()
}

/** Set of allowed language codes in this JVM. */
val languageCodes: Set<String> by lazy {
    Locale.getISOLanguages().toSet()
}

/** Set of allowed currency codes in this JVM. */
val currencyCodes: Set<String> by lazy {
    Currency.getAvailableCurrencies().map { it.currencyCode }.toSet()
}

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @param locale .
 * @return .
 */
inline fun <reified T : ResourceBundle> resourceBundle(
    locale: Locale = Locale.getDefault()): ResourceBundle =
    resourceBundle(T::class, locale)

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @param type .
 * @param locale .
 * @return .
 */
fun <T : ResourceBundle> resourceBundle(
    type: KClass<T>, locale: Locale = Locale.getDefault()): ResourceBundle =
    ResourceBundle.getBundle(type.java.name, locale)

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @param language .
 * @param country .
 * @return .
 */
fun localeOf(language: String = "", country: String = ""): Locale {
    require(language.isNotEmpty() || country.isNotEmpty()) {
        "A non-blank language or country is required"
    }
    require(language.isEmpty() || language in languageCodes) { "Language: '$language' not allowed" }
    require(country.isEmpty() || country in countryCodes) { "Country: '$country' not allowed" }
    return Locale(language, country)
}

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @param language .
 * @param country .
 * @return .
 */
fun localeOfOrNull(language: String = "", country: String = ""): Locale? =
    try {
        localeOf(language, country)
    }
    catch (_: IllegalArgumentException) {
        null
    }

fun languageOf(language: String): Locale =
    localeOf(language = language)

fun languageOfOrNull(language: String): Locale? =
    try {
        languageOf(language)
    }
    catch (_: IllegalArgumentException) {
        null
    }

fun countryOf(country: String): Locale =
    localeOf(country = country)

fun countryOfOrNull(country: String): Locale? =
    try {
        countryOf(country)
    }
    catch (_: IllegalArgumentException) {
        null
    }

fun parseLocale(languageCountry: String): Locale =
    languageCountry
        .split("_")
        .let { localeOf(it.getOrElse(0) { "" }, it.getOrElse(1) { "" }) }

fun parseLocaleOrNull(languageCountry: String): Locale? =
    try {
        parseLocale(languageCountry)
    }
    catch (_: IllegalArgumentException) {
        null
    }
