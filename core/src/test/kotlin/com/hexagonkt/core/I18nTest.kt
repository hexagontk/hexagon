package com.hexagonkt.core

import org.junit.jupiter.api.Test
import java.util.Currency.getAvailableCurrencies
import java.util.Locale.getISOCountries
import java.util.Locale.getISOLanguages
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

internal class I18nTest {

    @Test fun `All JVM countries are contained in the codes set`() {
        assertEquals(getISOCountries().toSet(), countryCodes)
    }

    @Test fun `All JVM languages are contained in the codes set`() {
        assertEquals(getISOLanguages().toSet(), languageCodes)
    }

    @Test fun `All JVM currencies are contained in the codes set`() {
        assertEquals(getAvailableCurrencies().map { it.currencyCode }.toSet(), currencyCodes)
    }

    @Test fun `Locales are created and checked correctly`() {
        assertEquals(
            "A non-blank language or country is required",
            assertFailsWith<IllegalArgumentException> { localeOf() }.message
        )
        assertEquals(
            "Language: '_' not allowed",
            assertFailsWith<IllegalArgumentException> { localeOf(language = "_") }.message
        )
        assertEquals(
            "Country: '#' not allowed",
            assertFailsWith<IllegalArgumentException> { localeOf(country = "#") }.message
        )
        assertEquals(
            "Language: '_' not allowed",
            assertFailsWith<IllegalArgumentException> { localeOf("_", "#") }.message
        )

        assertEquals(countryOf("ES"), localeOf(country = "ES"))
        assertEquals(languageOf("en"), localeOf(language = "en"))

        assertNull(countryOfOrNull("ZZ"))
        assertNull(languageOfOrNull("zz"))

        assertNull(localeOfOrNull())
        assertNull(localeOfOrNull("_"))
        assertNull(localeOfOrNull(country = "#"))
        assertNull(localeOfOrNull("_", "#"))

        assertEquals(countryOfOrNull("IT"), localeOfOrNull(country = "IT"))
        assertEquals(localeOfOrNull("fr"), localeOfOrNull(language = "fr"))
    }

    @Test fun `Locales are parsed correctly`() {
        assertEquals(localeOf("es", "ES"), parseLocale("es_ES"))
        assertEquals(languageOf("fr"), parseLocale("fr"))
        assertEquals(languageOf("fr"), parseLocale("fr_"))
        assertEquals(countryOf("US"), parseLocale("_US"))
        assertEquals(countryOf("US"), parseLocale("US"))

        assertFailsWith<IllegalArgumentException> { parseLocale("") }
        assertFailsWith<IllegalArgumentException> { parseLocale("_") }
        assertFailsWith<IllegalArgumentException> { parseLocale(" ") }
        assertFailsWith<IllegalArgumentException> { parseLocale("es _ ES") }
        assertFailsWith<IllegalArgumentException> { parseLocale("es _ES") }
        assertFailsWith<IllegalArgumentException> { parseLocale("es_ ES") }
        assertFailsWith<IllegalArgumentException> { parseLocale(" fr_FR ") }
        assertFailsWith<IllegalArgumentException> { parseLocale("en_fr_FR ") }

        assertEquals(localeOf("en", "GB"), parseLocaleOrNull("en_GB"))
        assertEquals(languageOf("it"), parseLocaleOrNull("it"))
        assertEquals(languageOf("it"), parseLocaleOrNull("it_"))
        assertEquals(countryOf("DE"), parseLocaleOrNull("_DE"))
        assertEquals(countryOf("DE"), parseLocaleOrNull("DE"))

        assertNull(parseLocaleOrNull(""))
        assertNull(parseLocaleOrNull("_"))
        assertNull(parseLocaleOrNull(" "))
        assertNull(parseLocaleOrNull("es _ ES"))
        assertNull(parseLocaleOrNull("es _ES"))
        assertNull(parseLocaleOrNull("es_ ES"))
        assertNull(parseLocaleOrNull(" fr_FR "))
    }
}
