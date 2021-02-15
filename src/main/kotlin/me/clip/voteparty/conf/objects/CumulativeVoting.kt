package me.clip.voteparty.conf.objects

internal data class CumulativeVoting(
    var daily: CumulativeVoteRewards = CumulativeVoteRewards(false, listOf(CumulativeVoteCommands(5, listOf("give %player_name% STEAK 10")))),
    var weekly: CumulativeVoteRewards = CumulativeVoteRewards(false, listOf(CumulativeVoteCommands(5, listOf("give %player_name% STEAK 10")))),
    var monthly: CumulativeVoteRewards = CumulativeVoteRewards(false, listOf(CumulativeVoteCommands(5, listOf("give %player_name% STEAK 10")))),
    var yearly: CumulativeVoteRewards = CumulativeVoteRewards(false, listOf(CumulativeVoteCommands(5, listOf("give %player_name% STEAK 10")))),
    var total: CumulativeVoteRewards = CumulativeVoteRewards(false, listOf(CumulativeVoteCommands(5, listOf("give %player_name% STEAK 10"))))
)