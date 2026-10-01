package me.clip.voteparty.leaderboard

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoField

enum class LeaderboardType(val end: () -> LocalDateTime, val displayName: String) {
    DAILY({ LocalDateTime.MIN }, "Daily") {
        override fun startOn(date: LocalDate): LocalDateTime = date.atStartOfDay()
    },
    WEEKLY({ LocalDateTime.MIN }, "Weekly") {
        override fun startOn(date: LocalDate): LocalDateTime = date.with(ChronoField.DAY_OF_WEEK, 1).atStartOfDay()
    },
    LASTMONTH(
        { LocalDate.now().with(ChronoField.DAY_OF_MONTH, 1).atStartOfDay() },
        "Last Month"
    ) {
        override fun startOn(date: LocalDate): LocalDateTime = date.minusMonths(1).with(ChronoField.DAY_OF_MONTH, 1).atStartOfDay()
    },
    MONTHLY({ LocalDateTime.MIN }, "Monthly") {
        override fun startOn(date: LocalDate): LocalDateTime = date.with(ChronoField.DAY_OF_MONTH, 1).atStartOfDay()
    },
    ANNUALLY({ LocalDateTime.MIN }, "Annually") {
        override fun startOn(date: LocalDate): LocalDateTime = date.with(ChronoField.DAY_OF_YEAR, 1).atStartOfDay()
    },
    ALLTIME({ LocalDateTime.MIN }, "All Time") {
        override fun startOn(date: LocalDate): LocalDateTime = date.with(ChronoField.EPOCH_DAY, 1).atStartOfDay()
    };

    /**
     * Where this period begins for today, for anything being read as of now: a leaderboard, a
     * placeholder, a reminder.
     *
     * A vote is not read as of now. It carries a timestamp, and a vote cast at 23:59:59.999 that is
     * worked out a millisecond later belongs to the day it was cast on, not to the one the clock
     * has since turned over to. [startAt] answers for the vote's own timestamp.
     */
    val start: () -> LocalDateTime = { startOn(LocalDate.now()) }

    /**
     * Where this period begins on a given day.
     */
    abstract fun startOn(date: LocalDate): LocalDateTime

    /**
     * Where the period a vote stamped [epoch] belongs to began, in the server's time zone.
     */
    internal fun startAt(epoch: Long): Long
    {
        val zone = ZoneId.systemDefault()
        val date = Instant.ofEpochMilli(epoch).atZone(zone).toLocalDate()

        return startOn(date).atZone(zone).toInstant().toEpochMilli()
    }

    companion object
    {
        internal val values = values()

        internal fun find(name: String): LeaderboardType?
        {
            return values.find { it.name.equals(name, true) }
        }
    }

}
