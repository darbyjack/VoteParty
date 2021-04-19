package me.clip.voteparty.conf.objects

internal data class CumulativeVoteCommands(var votes: Int = 5,
                                           var commands: List<String> = listOf(""))