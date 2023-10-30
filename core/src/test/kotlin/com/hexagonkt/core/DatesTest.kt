package com.hexagonkt.core

import org.junit.jupiter.api.Test
import java.time.*
import java.time.Month.*
import java.util.Calendar
import java.util.Calendar.MILLISECOND
import kotlin.test.assertEquals
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

    @Test fun `Period days are calculated correctly`() {
        assertEquals(1.0, parsePeriod("1d").toTotalDays())
        assertEquals(403.6875, parsePeriod("1y 1m 1w 1d").toTotalDays())
    }

    @Test fun `Parse periods work with not standard input`() {
        assertEquals(Period.parse("P1Y1W1D"), parsePeriod("P1Y1W1D"))
        assertEquals(Period.parse("P1Y1W1D"), parsePeriod("p1y1w1d"))
        assertEquals(Period.parse("P1Y1W1D"), parsePeriod("1y1w1d"))
        assertEquals(Period.parse("P1Y1W1D"), parsePeriod("1y 1w 1d"))
        assertEquals(Period.parse("P1Y1W1D"), parsePeriod("1y,1w,1d"))
        assertEquals(Period.parse("P1Y-1W-1D"), parsePeriod("1y-1w-1d"))
        assertEquals(Period.parse("P1Y1W1D"), parsePeriod("1y, 1w, 1d"))
    }

    @Test fun `Parse durations work with not standard input`() {
        assertEquals(Duration.parse("P1D"), parseDuration("P1D"))
        assertEquals(Duration.parse("P2D"), parseDuration("p2d"))
        assertEquals(Duration.parse("P3D"), parseDuration("3d"))
        assertEquals(Duration.parse("P-4D"), parseDuration("-4d"))

        assertEquals(Duration.parse("P1DT2H"), parseDuration("P1DT2H"))
        assertEquals(Duration.parse("P1DT2H"), parseDuration("p1dt2h"))
        assertEquals(Duration.parse("P1DT2H"), parseDuration("1dt2h"))
        assertEquals(Duration.parse("P1DT2H"), parseDuration("1d t 2h"))

        assertEquals(Duration.parse("P1DT3M"), parseDuration("1d t 3M"))
        assertEquals(Duration.parse("P1DT3M"), parseDuration("1d T 3m"))
        assertEquals(Duration.parse("P-1DT4.5S"), parseDuration("-1dT4.5S"))
        assertEquals(Duration.parse("P1DT6H7M8S"), parseDuration("1d t 6h7m8s"))
        assertEquals(Duration.parse("PT6H7M8S"), parseDuration("T 6H 7m,8S"))
    }

    @Test fun `parseLocalDate can handle years and years months assuming some defaults`() {
        assertEquals(LocalDate.of(1995, JANUARY, 1), parseLocalDate("1995"))
        assertEquals(LocalDate.of(1995, JANUARY, 1), parseLocalDate("1995-01"))
        assertEquals(LocalDate.of(1995, JANUARY, 1), parseLocalDate("1995-01-01"))
        assertEquals(LocalDate.of(1996, FEBRUARY, 1), parseLocalDate("1996-02"))
        assertEquals(LocalDate.of(1996, MARCH, 4), parseLocalDate("1996-03-04"))
    }
}
