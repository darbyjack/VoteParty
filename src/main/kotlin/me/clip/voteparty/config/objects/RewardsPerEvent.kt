package me.clip.voteparty.config.objects

data class RewardsPerEvent(var enabled: Boolean, var max_possible: Int, var commands: List<Command>)