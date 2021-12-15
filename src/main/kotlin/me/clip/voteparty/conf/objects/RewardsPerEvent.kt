package me.clip.voteparty.conf.objects

internal data class RewardsPerEvent(var enabled: Boolean = true,
                                    var max_possible: Int = 1,
                                    var commands: List<Command> = listOf(Command(50.0, listOf("eco give %player_name% 100")), Command(70.0, listOf("give %player_name% STEAK 10"))))
