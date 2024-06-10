package me.clip.voteparty.version

import org.bukkit.Color
import org.bukkit.Location
import org.inventivetalent.particle.ParticleEffect

class VersionHookOld : VersionHook
{
	
	override fun display(type: EffectType, location: Location, offsetX: Double, offsetY: Double, offsetZ: Double, speed: Double, count: Int, color: Color?)
	{
		val effect = resolve(type) ?: return
		
		if (color != null && effect.hasFeature(ParticleEffect.Feature.COLOR))
		{
			effect.sendColor(location.world.players, location, color)
		}
		else
		{
			effect.send(location.world.players, location, offsetX, offsetY, offsetZ, speed, count)
		}
	}
	
	
	private companion object
	{
		private val VALUES = ParticleEffect.entries.toTypedArray()
		
		
		private fun resolve(type: EffectType): ParticleEffect?
		{
			return VALUES.find { it.name.equals(type.name, true) }
		}
		
	}
	
}