package me.clip.voteparty.config.objects

internal data class Counter(var votes: Int = 0,
                   var save_interval: Int = 60)