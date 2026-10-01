package me.clip.voteparty.version

import org.bukkit.Color
import org.bukkit.Location
import org.inventivetalent.particle.ParticleEffect

class VersionHookOld : VersionHook
{
	
	override fun display(type: String, location: Location, offsetX: Double, offsetY: Double, offsetZ: Double, speed: Double, count: Int, color: Color?)
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
		/**
		 * The particle renames Minecraft made in 1.13, written the way ParticleAPI names them.
		 *
		 * ParticleAPI only knows the pre-1.13 generation of names, so a configuration written for
		 * a current server resolves to nothing here and the particle is silently dropped —
		 * including VoteParty's own default, which says SMOKE where ParticleAPI says
		 * SMOKE_NORMAL. Inverting the renames means one configuration means the same thing on
		 * 1.8 as it does on a current server.
		 *
		 * XSeries carries the same mapping but cannot be used for this: XParticle's class
		 * initialiser reads org.bukkit.Particle, which does not exist before 1.13, so touching it
		 * on a legacy server is a NoClassDefFoundError rather than a lookup miss.
		 *
		 * Where a modern name is ambiguous the entry picks the closest ParticleAPI effect: BLOCK
		 * covers block break and block dust, and resolves to the break effect.
		 */
		private val RENAMED = mapOf(
			"SMOKE" to "SMOKE_NORMAL",
			"LARGE_SMOKE" to "SMOKE_LARGE",
			"EXPLOSION" to "EXPLOSION_NORMAL",
			"FIREWORK" to "FIREWORKS_SPARK",
			"BUBBLE" to "WATER_BUBBLE",
			"SPLASH" to "WATER_SPLASH",
			"UNDERWATER" to "SUSPENDED",
			"ENCHANTED_HIT" to "CRIT_MAGIC",
			"ENTITY_EFFECT" to "SPELL_MOB",
			"WITCH" to "SPELL_WITCH",
			"DRIPPING_WATER" to "DRIP_WATER",
			"DRIPPING_LAVA" to "DRIP_LAVA",
			"ANGRY_VILLAGER" to "VILLAGER_ANGRY",
			"HAPPY_VILLAGER" to "VILLAGER_HAPPY",
			"ENCHANT" to "ENCHANTMENT_TABLE",
			"DUST" to "REDSTONE",
			"SNOWFLAKE" to "SNOW_SHOVEL",
			"ITEM_SLIME" to "SLIME",
			"ITEM" to "ITEM_CRACK",
			"BLOCK" to "BLOCK_CRACK",
			"TOTEM_OF_UNDYING" to "TOTEM"
		)
		
		
		private val BY_NAME: Map<String, ParticleEffect> = buildMap {
			ParticleEffect.entries.forEach()
			{ effect -> put(effect.name, effect) }
			
			RENAMED.forEach()
			{ (modern, legacy) ->
				ParticleEffect.entries.find { it.name == legacy }?.let()
				{ effect -> put(modern, effect) }
			}
		}
		
		
		private fun resolve(type: String): ParticleEffect?
		{
			return BY_NAME[type.trim().uppercase()]
		}
		
	}
	
}