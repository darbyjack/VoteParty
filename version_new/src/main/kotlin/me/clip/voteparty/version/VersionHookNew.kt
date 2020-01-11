package me.clip.voteparty.version

import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions

class VersionHookNew : VersionHook
{
	
	override fun display(type: EffectType, location: Location, offsetX: Double, offsetY: Double, offsetZ: Double, speed: Double, count: Int, color: Color?)
	{
		
		val world = location.world ?: return
		val particle = Particle.valueOf(type.name)
		
		if (particle.dataType == DustOptions::class.java)
		{
			return world.spawnParticle(particle, location, 1, if (color == null) OPTION else DustOptions(color, 0.8F))
		}
		
		when (particle)
		{
			in SINGLE     ->
			{
				world.spawnParticle(particle, location, count)
			}
			in SPELLS     ->
			{
				val r: Double
				val g: Double
				val b: Double
				
				if (color == null)
				{
					r = 0.0
					g = 0.0
					b = 0.0
				}
				else
				{
					r = color.red / 255.0
					g = color.green / 255.0
					b = color.blue / 255.0
				}
				
				world.spawnParticle(particle, location, count, r, g, b, 1)
			}
			Particle.NOTE ->
			{
				val note = if (color == null)
				{
					0.0
				}
				else
				{
					color.red / 24.0
				}
				
				world.spawnParticle(particle, location, count, note, offsetY, offsetZ, 1)
			}
			else          ->
			{
				world.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, 0.001)
			}
		}
	}
	
	private companion object
	{
		
		private val OPTION = DustOptions(Color.RED, 0.8F)
		
		private val SPELLS = setOf(
			Particle.SPELL_MOB,
			Particle.SPELL_MOB_AMBIENT
		                          )
		
		private val SINGLE = setOf(
			Particle.WATER_BUBBLE,
			Particle.WATER_WAKE,
			Particle.CRIT,
			Particle.CRIT_MAGIC,
			Particle.SMOKE_NORMAL,
			Particle.SMOKE_LARGE,
			Particle.PORTAL,
			Particle.ENCHANTMENT_TABLE,
			Particle.FLAME,
			Particle.CLOUD,
			Particle.DRAGON_BREATH,
			Particle.END_ROD,
			Particle.DAMAGE_INDICATOR,
			Particle.TOTEM,
			Particle.SPIT,
			Particle.SQUID_INK,
			Particle.BUBBLE_POP,
			Particle.BUBBLE_COLUMN_UP,
			Particle.NAUTILUS
		                          )
		
	}
	
}