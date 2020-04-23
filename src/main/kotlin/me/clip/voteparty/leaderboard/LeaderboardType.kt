package me.clip.voteparty.leaderboard

import java.time.Duration
import java.time.temporal.ChronoUnit

enum class LeaderboardType(val duration: Duration)
{
	DAILY(Duration.ofDays(1)),
	WEEKLY(Duration.ofDays(7)),
	MONTHLY(Duration.ofDays(30)),
	ALL_TIME(Duration.of(1, ChronoUnit.FOREVER))
}