package me.clip.voteparty.version

import org.bukkit.Color
import org.bukkit.Location

interface Version
{

	fun display(type: EffectType, location: Location, color: Color?)

}