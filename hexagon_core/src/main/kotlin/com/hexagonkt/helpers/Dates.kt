package com.hexagonkt.helpers

import java.time.*
import java.util.Date

/**
 * Converts a date time to a number with the following format: `YYYYMMDDHHmmss`.
 *
 * @receiver The date to be converted to a number.
 * @return Numeric representation of the given date.
 */
fun LocalDateTime.toNumber(): Long =
    (this.toLocalDate().toNumber() * 1_000_000_000L) + this.toLocalTime().toNumber()

/**
 * Converts a date to an integer with the following format: `YYYYMMDD`.
 *
 * @receiver The date to be converted to a number.
 * @return Numeric representation of the given date.
 */
fun LocalDate.toNumber(): Int =
    (this.year * 10_000) +
    (this.monthValue * 100) +
    this.dayOfMonth

/**
 * Converts a time to an integer with the following format: `HHmmssSSS`.
 *
 * @receiver The time to be converted to a number.
 * @return Numeric representation of the given time.
 */
fun LocalTime.toNumber(): Int =
    (this.hour * 10_000_000) +
    (this.minute * 100_000) +
    (this.second * 1_000) +
    (this.nano / 1_000_000) // Nanos to millis

/**
 * Returns the date time in a given time zone for a local date time.
 *
 * @receiver The local date time to be moved to another time zone.
 * @param zoneId Id of the target zone of the passed local date time.
 * @return The received date time at the given [zoneId].
 */
fun LocalDateTime.withZone(zoneId: ZoneId = Jvm.timeZone.toZoneId()): ZonedDateTime =
    ZonedDateTime.of(this, zoneId)

/**
 * Parses a date time from a formatted number with this format: `YYYYMMDDHHmmss`.
 *
 * @receiver Number to be converted to a date time.
 * @return Local date time representation of the given number.
 */
fun Long.toLocalDateTime(): LocalDateTime =
    (this / 1_000_000_000)
        .toInt()
        .toLocalDate()
        .atTime((this % 1_000_000_000).toInt().toLocalTime())

/**
 * Parses a date from a formatted integer with this format: `YYYYMMDD`.
 *
 * @receiver Number to be converted to a date.
 * @return Local date representation of the given number.
 */
fun Int.toLocalDate(): LocalDate =
    LocalDate.of(
        this / 10_000,
        (this % 10_000) / 100,
        this % 100
    )

/**
 * Parses a time from a formatted integer with this format: `HHmmssSSS`.
 *
 * @receiver Number to be converted to a time.
 * @return Local time representation of the given number.
 */
fun Int.toLocalTime(): LocalTime =
    LocalTime.of(
        (this / 10_000_000),
        ((this % 10_000_000) / 100_000),
        ((this % 100_000) / 1_000),
        ((this % 1_000) * 1_000_000) // Millis to nanos
    )

/**
 * .
 *
 * @receiver .
 * @return .
 */
fun ZonedDateTime.toDate(): Date =
    Date.from(this.toInstant())

fun LocalDateTime.toDate(): Date =
    this.atZone(Jvm.timeZone.toZoneId()).toDate()

fun LocalDate.toDate(): Date =
    this.atStartOfDay(Jvm.timeZone.toZoneId()).toDate()

fun Date.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this.time), ZoneId.systemDefault())

fun Date.toLocalDate(): LocalDate =
    this.toLocalDateTime().toLocalDate()
