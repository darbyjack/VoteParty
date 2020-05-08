package me.clip.voteparty.leaderboard

import me.clip.voteparty.user.User

data class LeaderboardUser(val user: User, var votes: Int)
{
	fun name() : String
	{
		return user.name
	}
}