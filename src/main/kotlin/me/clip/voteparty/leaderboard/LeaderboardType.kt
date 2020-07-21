package me.clip.voteparty.leaderboard

import me.clip.voteparty.exte.toMillis
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

enum class LeaderboardType(val time: () -> Long)
{
	DAILY({ toMillis(LocalDate.now().atStartOfDay()) }),
	WEEKLY({ toMillis(LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay()) }),
	MONTHLY({ toMillis(LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay()) }),
	ALLTIME({ toMillis(LocalDate.now().with(TemporalAdjusters.firstDayOfYear()).atStartOfDay()) });
	
	companion object
	{
		internal val values = values()
		
		internal fun find(name: String): LeaderboardType?
		{
			return values.find { it.name.equals(name, true) }
		}
	}

}