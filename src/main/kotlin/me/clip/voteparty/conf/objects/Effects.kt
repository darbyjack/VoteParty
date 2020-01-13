package me.clip.voteparty.conf.objects

internal data class Effects(var enable: Boolean = true,
                   var effects: List<String> = listOf("SMOKE_NORMAL", "HEART"),
                   var offsetX: Double = 0.0,
                   var offsetY: Double = 0.0,
                   var offsetZ: Double = 0.0,
                   var speed: Double = 0.0,
                   var count: Int = 3)
