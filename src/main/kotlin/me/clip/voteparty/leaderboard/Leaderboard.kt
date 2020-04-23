package me.clip.voteparty.leaderboard

import me.clip.voteparty.user.User

data class Leaderboard(val type: LeaderboardType, private val data: Map<User, Int>)