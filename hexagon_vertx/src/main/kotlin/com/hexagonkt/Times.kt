package com.hexagonkt

import java.time.*
import java.time.format.DateTimeFormatter.ISO_DATE_TIME
import java.util.*

/**
 * Returns a time difference in nanoseconds formatted as a string.
 */
fun formatNanos (timestamp: Long): String = "%1.3f ms".format (timestamp / 1e6)

fun LocalDateTime.formatToIso (): String = this.format(ISO_DATE_TIME)

fun LocalDateTime.withZone (zoneId: ZoneId = TimeZone.getDefault().toZoneId()): ZonedDateTime =
    ZonedDateTime.of(this, zoneId)

fun ZonedDateTime.toDate (): Date = Date.from(this.toInstant())

fun LocalDateTime.toDate (): Date = this.atZone(TimeZone.getDefault().toZoneId()).toDate()

fun LocalDate.toDate (): Date = this.atStartOfDay(TimeZone.getDefault().toZoneId()).toDate()

fun Date.toLocalDateTime (): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this.time), ZoneId.systemDefault())

fun Date.toLocalDate (): LocalDate = this.toLocalDateTime().toLocalDate()
