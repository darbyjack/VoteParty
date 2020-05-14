package me.clip.voteparty.leaderboard

data class Leaderboard(val type: LeaderboardType, val data: List<LeaderboardUser>)
{
	fun getEntry(entry: Int) : LeaderboardUser?
	{
		return if (entry < 1) data[0] else data[entry - 1]
	}
}