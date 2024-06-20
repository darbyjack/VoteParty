package me.clip.voteparty.version

import com.cryptomorin.xseries.particles.XParticle
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions

class VersionHookNew : VersionHook
{
	
	override fun display(type: EffectType, location: Location, offsetX: Double, offsetY: Double, offsetZ: Double, speed: Double, count: Int, color: Color?)
	{
		
		val world = location.world ?: return
		val optionalParticle = XParticle.of(type.name)

		if (!optionalParticle.isPresent)
		{
			return
		}

		val particle = optionalParticle.get()
		
		if (particle.get().dataType == DustOptions::class.java)
		{
			return world.spawnParticle(particle.get(), location, 1, if (color == null) OPTION else DustOptions(color, 0.8F))
		}
		
		when (particle)
		{
			in SINGLE     ->
			{
				world.spawnParticle(particle.get(), location, count)
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
				
				world.spawnParticle(particle.get(), location, count, r, g, b, 1)
			}
			XParticle.NOTE ->
			{
				val note = if (color == null)
				{
					0.0
				}
				else
				{
					color.red / 24.0
				}
				
				world.spawnParticle(particle.get(), location, count, note, offsetY, offsetZ, 1)
			}
			else          ->
			{
				world.spawnParticle(particle.get(), location, count, offsetX, offsetY, offsetZ, 0.001)
			}
		}
	}
	
	private companion object
	{
		
		private val OPTION = DustOptions(Color.RED, 0.8F)
		
		private val SPELLS = setOf(
			XParticle.ENTITY_EFFECT,
			)
		
		private val SINGLE = setOf(
			XParticle.BUBBLE,
			XParticle.FISHING,
			XParticle.CRIT,
			XParticle.ENCHANTED_HIT,
			XParticle.SMOKE,
			XParticle.LARGE_SMOKE,
			XParticle.PORTAL,
			XParticle.ENCHANT,
			XParticle.FLAME,
			XParticle.CLOUD,
			XParticle.DRAGON_BREATH,
			XParticle.END_ROD,
			XParticle.DAMAGE_INDICATOR,
			XParticle.TOTEM_OF_UNDYING,
			XParticle.SPIT,
			XParticle.SQUID_INK,
			XParticle.BUBBLE_POP,
			XParticle.BUBBLE_COLUMN_UP,
			XParticle.NAUTILUS
		                          )
		
	}
	
}