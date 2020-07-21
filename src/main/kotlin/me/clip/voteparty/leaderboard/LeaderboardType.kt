package me.clip.voteparty.leaderboard

import java.time.Duration

enum class LeaderboardType(val duration: Duration)
{
	DAILY(Duration.ofDays(1)),
	WEEKLY(Duration.ofDays(7)),
	MONTHLY(Duration.ofDays(30)),
	ALLTIME(Duration.ofDays(365));
	
	companion object
	{
		internal val values = values()
		
		internal fun find(name: String): LeaderboardType?
		{
			return values.find { it.name.equals(name, true) }
		}
	}
}