package me.clip.voteparty.version

import org.bukkit.Color
import org.bukkit.Location

interface VersionHook
{
	
	fun display(type: EffectType, location: Location, color: Color?, offsetX: Double, offsetY: Double, offsetZ: Double, speed: Double, count: Int)
	
}