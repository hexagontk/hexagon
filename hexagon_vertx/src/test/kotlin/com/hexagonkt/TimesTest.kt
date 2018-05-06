package com.hexagonkt

import org.junit.Test
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalDateTime.of as dateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter.ISO_DATE_TIME
import java.util.*
import java.util.Calendar.MILLISECOND

class TimesTest {

    @Test fun `Time nanos gets the elapsed nanoseconds` () {
        val nanos = System.nanoTime()
        val timeNanos = formatNanos(nanos)

        val decimalSeparator = DecimalFormat().decimalFormatSymbols.decimalSeparator
        assert (timeNanos.endsWith("ms") && timeNanos.contains(decimalSeparator))
    }

    @Test fun `Date conversion`() {
        val cal = Calendar.getInstance()
        cal.set(2017, 11, 31, 0, 0, 0)
        cal.set(MILLISECOND, 0)
        val d = cal.time
        val ld = LocalDate.of(2017, 12, 31)

        assert(ld.toDate() == d)
        assert(ld == d.toLocalDate())
    }

    @Test fun `Format date`() {
        val now = LocalDateTime.now()
        assert(now.formatToIso() == now.format(ISO_DATE_TIME))
    }

    @Test fun `Zoned date`() {
        val now = LocalDateTime.now()
        assert(now.withZone(ZoneId.of("GMT")).toLocalDateTime() == now)
    }

    @Test fun `LocalDateTime can be converted to Date`() {
        val now = LocalDateTime.of(2018, 12, 31, 23, 59, 59)
        assert(now.toDate().toLocalDateTime() == now)
    }
}
