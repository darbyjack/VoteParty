package me.clip.voteparty.conf.objects

internal data class PermRewards(var enabled: Boolean = true,
                                var permCommands: List<PermCommands> = listOf(PermCommands("my.special.permission", listOf("eco give %player_name% 500"))))