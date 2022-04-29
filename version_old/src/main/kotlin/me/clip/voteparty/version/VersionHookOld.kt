package me.clip.voteparty.version

import org.bukkit.Color
import org.bukkit.Location
import xyz.xenondevs.particle.ParticleBuilder
import xyz.xenondevs.particle.ParticleEffect
import xyz.xenondevs.particle.PropertyType

class VersionHookOld : VersionHook
{
	
	override fun display(type: EffectType, location: Location, offsetX: Double, offsetY: Double, offsetZ: Double, speed: Double, count: Int, color: Color?)
	{
		val effect = resolve(type) ?: return

		if (color != null && effect.hasProperty(PropertyType.COLORABLE))
		{
			ParticleBuilder(effect)
				.setLocation(location)
				.setColor(java.awt.Color(color.red, color.green, color.blue))
				.display(location.world.players)

			//effect.display(location, RegularColor(color.red, color.green, color.blue), location.world.players)
			//effect.sendColor(location.world.players, location, color)
		}
		else
		{
			ParticleBuilder(effect)
				.setLocation(location)
				.setOffsetX(offsetX.toFloat())
				.setOffsetY(offsetY.toFloat())
				.setOffsetZ(offsetZ.toFloat())
				.setSpeed(speed.toFloat())
				.setAmount(count)
				.display(location.world.players)


			//effect.display(location, offsetX.toFloat(), offsetY.toFloat(), offsetZ.toFloat(), speed.toFloat(), count, null, location.world.players)
			//effect.send(location.world.players, location, offsetX, offsetY, offsetZ, speed, count)
		}
	}
	
	
	private companion object
	{
		private val VALUES = ParticleEffect.values()
		
		
		private fun resolve(type: EffectType): ParticleEffect?
		{
			return VALUES.find { it.name.equals(type.name, true) }
		}
		
	}
	
}
