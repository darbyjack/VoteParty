package me.clip.voteparty.leaderboard

data class Leaderboard(val type: LeaderboardType, val data: List<LeaderboardUser>)
{
	fun getEntry(entry: Int) : LeaderboardUser?
	{
        return when {
            entry < 1 ->
            {
                data[0]
            }
            entry > data.size ->
            {
                null
            }
            else ->
            {
                data[entry - 1]
            }
        }
	}
}