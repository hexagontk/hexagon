package com.hexagonkt.core

import java.time.*
import java.util.Date

private const val DATE_OFFSET: Long = 1_000_000_000L
private const val YEAR_OFFSET: Int = 10_000
private const val MONTH_OFFSET: Int = 100
private const val HOUR_OFFSET: Int = 10_000_000
private const val MINUTE_OFFSET: Int = 100_000
private const val SECOND_OFFSET: Int = 1_000
private const val NANO_OFFSET: Int = 1_000_000

/**
 * Convert a date time to a number with the following format: `YYYYMMDDHHmmss`.
 *
 * @receiver Date to be converted to a number.
 * @return Numeric representation of the given date.
 */
fun LocalDateTime.toNumber(): Long =
    (this.toLocalDate().toNumber() * DATE_OFFSET) + this.toLocalTime().toNumber()

/**
 * Convert a date to an integer with the following format: `YYYYMMDD`.
 *
 * @receiver Date to be converted to a number.
 * @return Numeric representation of the given date.
 */
fun LocalDate.toNumber(): Int =
    (this.year * YEAR_OFFSET) +
    (this.monthValue * MONTH_OFFSET) +
    this.dayOfMonth

/**
 * Convert a time to an integer with the following format: `HHmmssSSS`.
 *
 * @receiver Time to be converted to a number.
 * @return Numeric representation of the given time.
 */
fun LocalTime.toNumber(): Int =
    (this.hour * HOUR_OFFSET) +
    (this.minute * MINUTE_OFFSET) +
    (this.second * SECOND_OFFSET) +
    (this.nano / NANO_OFFSET) // Nanos to millis

/**
 * Return the date time in a given time zone for a local date time.
 *
 * @receiver Local date time to be moved to another time zone.
 * @param zoneId Id of the target zone of the passed local date time.
 * @return Received date time at the given [zoneId].
 */
fun LocalDateTime.withZone(zoneId: ZoneId = Jvm.timeZone.toZoneId()): ZonedDateTime =
    ZonedDateTime.of(this, zoneId)

/**
 * Parse a date time from a formatted number with this format: `YYYYMMDDHHmmss`.
 *
 * @receiver Number to be converted to a date time.
 * @return Local date time representation of the given number.
 */
fun Long.toLocalDateTime(): LocalDateTime {
    require(this >= 0) { "Number representing timestamp must be positive (format: YYYYMMDDHHmmss)" }
    return (this / DATE_OFFSET)
        .toInt()
        .toLocalDate()
        .atTime((this % DATE_OFFSET).toInt().toLocalTime())
}

/**
 * Parse a date from a formatted integer with this format: `YYYYMMDD`.
 *
 * @receiver Number to be converted to a date.
 * @return Local date representation of the given number.
 */
fun Int.toLocalDate(): LocalDate {
    require(this >= 0) { "Number representing date must be positive (format: YYYYMMDD)" }
    return LocalDate.of(
        this / YEAR_OFFSET,
        (this % YEAR_OFFSET) / MONTH_OFFSET,
        this % MONTH_OFFSET
    )
}

/**
 * Parse a time from a formatted integer with this format: `HHmmssSSS`.
 *
 * @receiver Number to be converted to a time.
 * @return Local time representation of the given number.
 */
fun Int.toLocalTime(): LocalTime {
    require(this >= 0) { "Number representing time must be positive (format: HHmmssSSS)" }
    return LocalTime.of(
        (this / HOUR_OFFSET),
        ((this % HOUR_OFFSET) / MINUTE_OFFSET),
        ((this % MINUTE_OFFSET) / SECOND_OFFSET),
        ((this % SECOND_OFFSET) * NANO_OFFSET) // Millis to nanos
    )
}

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
    this.atZone(Jvm.timeZone.toZoneId()).toDate()

/**
 * Convert a local date to a date.
 *
 * @receiver Local date to be converted to a date.
 * @return Date representation of the given local date.
 */
fun LocalDate.toDate(): Date =
    this.atStartOfDay(Jvm.timeZone.toZoneId()).toDate()

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
 * Parse a time period allowing a more relaxed format: with spaces, hyphens or commas, lowercase
 * characters and not forcing the text to start with 'P'.
 *
 * @param text Text to be parsed to a time period.
 * @return Time period parsed from the supplied text.
 */
fun parsePeriod(text: String): Period =
    text.replace(",", "").replace("-", "").replace(" ", "").uppercase().let {
        Period.parse(
            if (it.startsWith("P")) it
            else "P$it"
        )
    }

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
