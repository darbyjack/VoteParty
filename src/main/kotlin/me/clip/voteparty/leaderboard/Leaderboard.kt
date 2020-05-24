package me.clip.voteparty.leaderboard

data class Leaderboard(val type: LeaderboardType, val data: List<LeaderboardUser>)
{
	fun getEntry(entry: Int) : LeaderboardUser?
	{
		return data.getOrNull((entry - 1).coerceAtLeast(0))
	}
}