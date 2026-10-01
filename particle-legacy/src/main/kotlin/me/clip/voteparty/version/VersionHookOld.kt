package me.clip.voteparty.version

import org.bukkit.Color
import org.bukkit.Location
import org.inventivetalent.particle.ParticleEffect

class VersionHookOld : VersionHook
{
	
	override fun display(type: String, location: Location, offsetX: Double, offsetY: Double, offsetZ: Double, speed: Double, count: Int, color: Color?)
	{
		val effect = resolve(type) ?: return
		
		// Two ways a resolved effect still cannot be drawn on this server. ParticleAPI refuses its
		// parameterless send() for effects that carry block or item data — BLOCK_CRACK, REDSTONE,
		// ITEM_CRACK and friends all throw ParticleException from it — and this configuration has
		// offsets, speed and count with no way to supply block or item data. And an effect can sit
		// in ParticleAPI's enum while still needing a newer server than the one running: TOTEM needs
		// 1.11. Neither is a failure, they are particles this server has no way to draw.
		if (effect.hasFeature(ParticleEffect.Feature.DATA) || !effect.isCompatible())
		{
			return
		}
		
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
		 * Every post-1.13 particle name mapped back to the ParticleAPI name for the same particle.
		 *
		 * ParticleAPI only knows the pre-1.13 generation of names, so a configuration written for
		 * a current server resolves to nothing here and the particle is silently dropped — including
		 * VoteParty's own default, which says SMOKE where ParticleAPI says SMOKE_NORMAL. This means
		 * one configuration means the same thing on 1.8 as it does on a current server.
		 *
		 * Taken from XSeries 13.7.1's own particle aliases rather than written from memory: every
		 * entry here is a name XParticle answers to, paired with the ParticleAPI effect that name
		 * replaced. XSeries cannot be used to do the lookup at runtime, because XParticle's class
		 * initialiser reads org.bukkit.Particle, which does not exist before 1.13 — touching it on
		 * a legacy server is a NoClassDefFoundError during plugin enable, not a lookup miss.
		 *
		 * Four modern names cover more than one old effect, and take the one that matches the modern
		 * particle rather than one of its variants: BLOCK over BLOCK_DUST, ENTITY_EFFECT over
		 * SPELL_MOB_AMBIENT, ITEM_SNOWBALL over SNOW_SHOVEL, UNDERWATER over SUSPENDED_DEPTH.
		 * ParticleAPI names that never changed — CLOUD, CRIT, HEART, NOTE, PORTAL, FLAME, LAVA,
		 * SPELL_MOB's siblings, and the rest — resolve through the plain name index instead.
		 */
		private val RENAMED = mapOf(
			"ANGRY_VILLAGER" to "VILLAGER_ANGRY",
			"BLOCK" to "BLOCK_CRACK",
			"BLOCK_MARKER" to "BARRIER",
			"BUBBLE" to "WATER_BUBBLE",
			"DRIPPING_LAVA" to "DRIP_LAVA",
			"DRIPPING_WATER" to "DRIP_WATER",
			"DUST" to "REDSTONE",
			"EFFECT" to "SPELL",
			"ELDER_GUARDIAN" to "MOB_APPEARANCE",
			"ENCHANT" to "ENCHANTMENT_TABLE",
			"ENCHANTED_HIT" to "CRIT_MAGIC",
			"ENTITY_EFFECT" to "SPELL_MOB",
			"EXPLOSION" to "EXPLOSION_LARGE",
			"EXPLOSION_EMITTER" to "EXPLOSION_HUGE",
			"FIREWORK" to "FIREWORKS_SPARK",
			"FISHING" to "WATER_WAKE",
			"HAPPY_VILLAGER" to "VILLAGER_HAPPY",
			"INSTANT_EFFECT" to "SPELL_INSTANT",
			"ITEM" to "ITEM_CRACK",
			"ITEM_SLIME" to "SLIME",
			"ITEM_SNOWBALL" to "SNOWBALL",
			"LARGE_SMOKE" to "SMOKE_LARGE",
			"MYCELIUM" to "TOWN_AURA",
			"POOF" to "EXPLOSION_NORMAL",
			"RAIN" to "WATER_DROP",
			"SMOKE" to "SMOKE_NORMAL",
			"SPLASH" to "WATER_SPLASH",
			"TOTEM_OF_UNDYING" to "TOTEM",
			"UNDERWATER" to "SUSPENDED",
			"WITCH" to "SPELL_WITCH",
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