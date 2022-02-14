package com.hexagonkt.core

import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Calendar
import java.util.Calendar.MILLISECOND
import kotlin.test.assertFailsWith
import java.time.LocalDateTime.of as dateTime

internal class DatesTest {

    @Test fun `Date conversion`() {
        val cal = Calendar.getInstance()
        cal.set(2017, 11, 31, 0, 0, 0)
        cal.set(MILLISECOND, 0)
        val d = cal.time
        val ld = LocalDate.of(2017, 12, 31)

        assert(ld.toDate() == d)
        assert(ld == d.toLocalDate())
    }

    @Test fun `Zoned date`() {
        val now = LocalDateTime.now()
        assert(now.withZone(ZoneId.of("GMT")).toLocalDateTime() == now)
        assert(now.withZone() == ZonedDateTime.of(now, Jvm.timeZone.toZoneId()))
    }

    @Test fun `LocalDateTime can be converted to Date`() {
        val now = LocalDateTime.of(2018, 12, 31, 23, 59, 59)
        assert(now.toDate().toLocalDateTime() == now)
    }

    @Test fun `A local date time returns a valid int timestamp` () {
        assert(dateTime (2015, 12, 31, 23, 59, 59).toNumber() == 2015_12_31_23_59_59_000)
        assert(dateTime (2015, 12, 31, 23, 59, 59, 101000000).toNumber() == 2015_12_31_23_59_59_101)
        assert(dateTime (2015, 12, 31, 23, 59, 59, 101000000).toNumber() != 2015_12_31_23_59_59_100)
    }

    @Test fun `Dates are parsed from ints`() {
        assert(2016_09_05_17_45_59_101.toLocalDateTime() ==
            LocalDateTime.of(2016, 9, 5, 17, 45, 59, 101_000_000))
        assert(2016_09_05_17_45_58_101.toLocalDateTime() !=
            LocalDateTime.of(2016, 9, 5, 17, 45, 59, 101_000_000))
    }

    @Test fun `Negative numbers raise error when converted to dates`() {
        assertFailsWith<IllegalArgumentException> { (-1L).toLocalDateTime() }
        assertFailsWith<IllegalArgumentException> { (-1).toLocalDate() }
        assertFailsWith<IllegalArgumentException> { (-1).toLocalTime() }
    }
}
