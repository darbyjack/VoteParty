package me.clip.voteparty.conf.objects

internal data class VotesiteRewards(var enabled: Boolean = true,
                                    var votesiteCommands: List<VotesiteCommands> = listOf(VotesiteCommands("TestVote", listOf("eco give %player_name% 500"))))