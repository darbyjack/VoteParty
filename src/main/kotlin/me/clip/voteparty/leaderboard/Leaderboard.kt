package me.clip.voteparty.leaderboard

import me.clip.voteparty.user.User

data class Leaderboard(val type: LeaderboardType, val data: List<LeaderboardUser>)
{
	fun getEntry(entry: Int) : LeaderboardUser?
	{
		return data.getOrNull((entry - 1).coerceAtLeast(0))
	}

	private fun getUser(user: User) : LeaderboardUser?
	{
		return data.firstOrNull{ user.name == it.name() }
	}

	fun getPlacement(user: User) : Int?
	{
		return data.indexOfFirst { getUser(user) == it }
	}
}