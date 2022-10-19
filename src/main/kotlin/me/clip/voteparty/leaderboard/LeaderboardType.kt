package me.clip.voteparty.leaderboard

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoField

enum class LeaderboardType(val start: () -> LocalDateTime, val end: () -> LocalDateTime)
{
	DAILY(
		{ LocalDate.now().atStartOfDay() },
		{ LocalDate.now().plusDays(1).atStartOfDay() }
	),
	WEEKLY(
		{ LocalDate.now().with(ChronoField.DAY_OF_WEEK, 1).atStartOfDay() },
		{ LocalDate.now().with(ChronoField.DAY_OF_WEEK, 1).plusWeeks(1).atStartOfDay() }
	),
	LASTMONTH(
		{ LocalDate.now().with(ChronoField.DAY_OF_MONTH, 1).minusMonths(1).atStartOfDay() },
		{ LocalDate.now().with(ChronoField.DAY_OF_MONTH, 1).atStartOfDay() }
	),
	MONTHLY(
		{ LocalDate.now().with(ChronoField.DAY_OF_MONTH, 1).atStartOfDay() },
		{ LocalDate.now().with(ChronoField.DAY_OF_MONTH, 1).plusMonths(1).atStartOfDay() }
	),
	ANNUALLY(
		{ LocalDate.now().with(ChronoField.DAY_OF_YEAR, 1).atStartOfDay() },
		{ LocalDate.now().with(ChronoField.DAY_OF_YEAR, 1).plusYears(1).atStartOfDay() }
	),
	ALLTIME(
		{ LocalDate.now().with(ChronoField.EPOCH_DAY, 1).atStartOfDay() },
		{ LocalDate.now().with(ChronoField.EPOCH_DAY, 1).plusYears(137).atStartOfDay() }
	);

	init {
		if (end.invoke() <= start.invoke()) {
			throw IllegalArgumentException("end time must be greater than start time")
		}
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
