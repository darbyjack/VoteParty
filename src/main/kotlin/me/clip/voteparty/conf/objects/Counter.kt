package me.clip.voteparty.conf.objects

internal data class Counter(var votes: Int = 0,
                   var save_interval: Int = 60)