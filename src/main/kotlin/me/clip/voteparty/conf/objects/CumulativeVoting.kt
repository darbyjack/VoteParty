package me.clip.voteparty.conf.objects

internal data class CumulativeVoting(
    var daily: CumulativeVoteRewards = CumulativeVoteRewards(false, listOf(CumulativeVoteCommands(5, listOf("test")))),
    var weekly: CumulativeVoteRewards = CumulativeVoteRewards(false, listOf(CumulativeVoteCommands(5, listOf("test")))),
    var monthly: CumulativeVoteRewards = CumulativeVoteRewards(false, listOf(CumulativeVoteCommands(5, listOf("test")))),
    var total: CumulativeVoteRewards = CumulativeVoteRewards(false, listOf(CumulativeVoteCommands(5, listOf("test"))))
)