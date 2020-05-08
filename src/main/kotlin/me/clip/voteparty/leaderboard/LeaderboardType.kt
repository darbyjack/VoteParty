package me.clip.voteparty.leaderboard

import java.time.Duration

enum class LeaderboardType(val duration: Duration)
{
	DAILY(Duration.ofDays(1)),
	WEEKLY(Duration.ofDays(7)),
	MONTHLY(Duration.ofDays(30)),
	ALL_TIME(Duration.ofDays(365))
}