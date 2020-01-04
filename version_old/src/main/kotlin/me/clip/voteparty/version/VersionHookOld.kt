package me.clip.voteparty.version

import org.bukkit.Color
import org.bukkit.Location
import org.inventivetalent.particle.ParticleEffect
import java.util.Arrays

class VersionHookOld : VersionHook
{
	
	override fun display(type: EffectType, location: Location, color: Color?)
	{
		val effect = resolve(type)
		if (color != null && effect.hasFeature(ParticleEffect.Feature.COLOR))
		{
			effect.sendColor(location.world.players, location, color)
		}
		else
		{
			effect.send(location.world.players, location, 0.0, 0.0, 0.0, 0.0, 1)
		}
	}
	
	private fun resolve(type: EffectType): ParticleEffect
	{
		return Arrays.stream(ParticleEffect.values()).filter {
			it.name.equals(type.name, ignoreCase = true)
		}.findFirst().orElse(null)
	}
	
}