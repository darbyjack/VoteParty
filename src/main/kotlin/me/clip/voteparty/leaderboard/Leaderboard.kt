package me.clip.voteparty.leaderboard

data class Leaderboard(val type: LeaderboardType, val data: List<LeaderboardUser>)
{
	fun getEntry(index: Int) : LeaderboardUser
	{
		return data[index - 1]
	}
}