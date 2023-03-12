package me.clip.voteparty.leaderboard

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoField

enum class LeaderboardType(val start: () -> LocalDateTime, val end: () -> LocalDateTime, val displayName: String) {
    DAILY(
        { LocalDate.now().atStartOfDay() },
        { LocalDateTime.MIN },
        "Daily"
    ),
    WEEKLY(
        { LocalDate.now().with(ChronoField.DAY_OF_WEEK, 1).atStartOfDay() },
        { LocalDateTime.MIN },
        "Weekly"
    ),
    LASTMONTH(
        { LocalDate.now().minusMonths(1).with(ChronoField.DAY_OF_MONTH, 1).atStartOfDay() },
        { LocalDate.now().with(ChronoField.DAY_OF_MONTH, 1).atStartOfDay() },
        "Last Month"
    ),
    MONTHLY(
        { LocalDate.now().with(ChronoField.DAY_OF_MONTH, 1).atStartOfDay() },
        { LocalDateTime.MIN },
        "Monthly"
    ),
    ANNUALLY(
        { LocalDate.now().with(ChronoField.DAY_OF_YEAR, 1).atStartOfDay() },
        { LocalDateTime.MIN },
        "Annually"
    ),
    ALLTIME(
        { LocalDate.now().with(ChronoField.EPOCH_DAY, 1).atStartOfDay() },
        { LocalDateTime.MIN },
        "All Time"
    );

    companion object
    {
        internal val values = values()

        internal fun find(name: String): LeaderboardType?
        {
            return values.find { it.name.equals(name, true) }
        }
    }

}
