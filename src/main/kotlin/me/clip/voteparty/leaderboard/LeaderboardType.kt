package me.clip.voteparty.leaderboard

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoField

enum class LeaderboardType(val time: () -> LocalDateTime)
{
	DAILY({ LocalDate.now().atStartOfDay() }),
	WEEKLY({ LocalDate.now().with(ChronoField.DAY_OF_WEEK, 1).atStartOfDay() }),
	MONTHLY({ LocalDate.now().with(ChronoField.DAY_OF_MONTH, 1).atStartOfDay() }),
	ANNUALLY({ LocalDate.now().with(ChronoField.DAY_OF_YEAR, 1).atStartOfDay() }),
	ALLTIME({ LocalDate.now().with(ChronoField.EPOCH_DAY, 1).atStartOfDay() });
	
	companion object
	{
		internal val values = values()
		
		internal fun find(name: String): LeaderboardType?
		{
			return values.find { it.name.equals(name, true) }
		}
	}

}
