package com.hexagontk.core

import java.time.*
import java.util.Date

private const val DAYS_PER_MONTH: Double = 30.4375

/** GMT zone ID. */
val GMT_ZONE: ZoneId by lazy { ZoneId.of("GMT") }

/**
 * Return the date time in a given time zone for a local date time.
 *
 * @receiver Local date time to be moved to another time zone.
 * @param zoneId Id of the target zone of the passed local date time.
 * @return Received date time at the given [zoneId].
 */
fun LocalDateTime.withZone(zoneId: ZoneId = Platform.timeZone.toZoneId()): ZonedDateTime =
    ZonedDateTime.of(this, zoneId)

/**
 * Convert a zoned date time to a date.
 *
 * @receiver Zoned date time to be converted to a date.
 * @return Date representation of the given zoned date time.
 */
fun ZonedDateTime.toDate(): Date =
    Date.from(this.toInstant())

/**
 * Convert a local date time to a date.
 *
 * @receiver Local date time to be converted to a date.
 * @return Date representation of the given local date time.
 */
fun LocalDateTime.toDate(): Date =
    this.atZone(Platform.timeZone.toZoneId()).toDate()

/**
 * Convert a local date to a date.
 *
 * @receiver Local date to be converted to a date.
 * @return Date representation of the given local date.
 */
fun LocalDate.toDate(): Date =
    this.atStartOfDay(Platform.timeZone.toZoneId()).toDate()

/**
 * Convert a date to a local date time.
 *
 * @receiver Date to be converted to a local date time.
 * @return Local date time representation of the given date.
 */
fun Date.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this.time), ZoneId.systemDefault())

/**
 * Convert a date to a local date.
 *
 * @receiver Date to be converted to a local date.
 * @return Local date representation of the given date.
 */
fun Date.toLocalDate(): LocalDate =
    this.toLocalDateTime().toLocalDate()

/**
 * Calculate the aproximate number of days comprised in a time period.
 *
 * @receiver Period from which calculate the number of days.
 * @return Aproximate number of days of the period.
 */
fun Period.toTotalDays(): Double =
    (toTotalMonths() * DAYS_PER_MONTH) + days

/**
 * Parse a time period allowing a more relaxed format: with spaces or commas, lowercase characters
 * and not forcing the text to start with 'P'.
 *
 * @param text Text to be parsed to a time period.
 * @return Time period parsed from the supplied text.
 */
fun parsePeriod(text: String): Period =
    Period.parse(formatDuration(text))

/**
 * Parse a time duration allowing a more relaxed format: with spaces or commas, lowercase characters
 * and not forcing the text to start with 'P', however, the 'T' is still mandatory to separate date
 * and time durations.
 *
 * @param text Text to be parsed to a time duration.
 * @return Time duration parsed from the supplied text.
 */
fun parseDuration(text: String): Duration =
    Duration.parse(formatDuration(text))

private fun formatDuration(text: String): String =
    text
        .replace(",", "")
        .replace(" ", "")
        .uppercase()
        .let { if (it.startsWith("P")) it else "P$it" }

/**
 * Parse a local date allowing only to specify the year or the year and the month. Missing month and
 * day will be defaulted to january and one respectively.
 *
 * @param text Text to be parsed to a local date.
 * @return Local date parsed from the supplied text.
 */
fun parseLocalDate(text: String): LocalDate =
    when (text.length) {
        4 -> Year.parse(text).atMonth(1).atDay(1)
        7 -> YearMonth.parse(text).atDay(1)
        else -> LocalDate.parse(text)
    }
