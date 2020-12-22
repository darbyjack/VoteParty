package me.clip.voteparty.conf.objects

internal data class CumulativeVoteRewards(
	var enabled: Boolean = false,
	var entries: List<CumulativeVoteCommands> = listOf(CumulativeVoteCommands(5, listOf("eco give %player_name% 500")))
)