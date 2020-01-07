package me.clip.voteparty.version

import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions


class VersionHookNew : VersionHook
{
	
	override fun display(type: EffectType, location: Location, color: Color?, offsetX: Double, offsetY: Double, offsetZ: Double, speed: Double, count: Int)
	{
		val particle = Particle.valueOf(type.name)
		if (particle.dataType == DustOptions::class.java)
		{
			if (color == null)
			{
				location.world?.spawnParticle(particle, location, 1, DustOptions(Color.RED, 0.8F)) // constant new Particle.DustOptions(Color.RED, 0.8F);
			}
			else
			{
				location.world?.spawnParticle(particle, location, 1, DustOptions(color, 0.8F)) // cached new Particle.DustOptions(color, 0.8F)
			}
			return
		}
		when (particle)
		{
			Particle.WATER_BUBBLE, Particle.WATER_WAKE, Particle.CRIT,
			Particle.CRIT_MAGIC, Particle.SMOKE_NORMAL, Particle.SMOKE_LARGE,
			Particle.PORTAL, Particle.ENCHANTMENT_TABLE, Particle.FLAME,
			Particle.CLOUD, Particle.DRAGON_BREATH, Particle.END_ROD,
			Particle.DAMAGE_INDICATOR, Particle.TOTEM, Particle.SPIT,
			Particle.SQUID_INK, Particle.BUBBLE_POP, Particle.BUBBLE_COLUMN_UP,
			Particle.NAUTILUS                              -> location.world?.spawnParticle(particle, location, count)
			Particle.NOTE                                  ->
			{
				val note = color?.red?.div(24.0) ?: 0.0
				location.world?.spawnParticle(particle, location, count, note, offsetY, offsetZ, 1)
			}
			Particle.SPELL_MOB, Particle.SPELL_MOB_AMBIENT ->
			{
				val r = color?.red?.div(255.0) ?: 0.0
				val g = color?.green?.div(255.0) ?: 0.0
				val b = color?.blue?.div(255.0) ?: 0.0
				location.world?.spawnParticle(particle, location, count, r, g, b, 1)
			}
			else                                           -> location.world?.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, 0.001)
		}
	}
	
}