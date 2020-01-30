package me.clip.voteparty.handler

import me.clip.voteparty.base.Addon
import me.clip.voteparty.conf.sections.CrateSettings
import me.clip.voteparty.conf.sections.EffectsSettings
import me.clip.voteparty.conf.sections.PartySettings
import me.clip.voteparty.exte.color
import me.clip.voteparty.exte.formMessage
import me.clip.voteparty.exte.meta
import me.clip.voteparty.exte.name
import me.clip.voteparty.exte.runTaskLater
import me.clip.voteparty.exte.runTaskTimer
import me.clip.voteparty.exte.takeRandomly
import me.clip.voteparty.plugin.VotePartyPlugin
import me.clip.voteparty.version.EffectType
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class PartyHandler(override val plugin: VotePartyPlugin) : Addon
{
	
	fun giveRandomPartyRewards(player: Player)
	{
		val settings = party.conf().getProperty(PartySettings.REWARD_COMMANDS)
		
		if (!settings.enabled || settings.max_possible <= 0 || settings.commands.isEmpty())
		{
			return
		}
		
		val iter = settings.commands.takeRandomly(settings.max_possible).iterator()
		
		plugin.runTaskTimer(party.conf().getProperty(PartySettings.COMMAND_DELAY).toLong() * 20L)
		{
			if (!iter.hasNext())
			{
				return@runTaskTimer cancel()
			}
			
			runPartyCommandEffects(player)
			
			iter.next().command.forEach()
			{
				server.dispatchCommand(server.consoleSender, formMessage(player, it))
			}
		}
	}
	
	fun giveGuaranteedPartyRewards(player: Player)
	{
		val settings = party.conf().getProperty(PartySettings.GUARANTEED_REWARDS)
		
		executeCommands(settings.enabled, settings.commands, player)
	}
	
	fun runPrePartyCommands()
	{
		val settings = party.conf().getProperty(PartySettings.PRE_PARTY_COMMANDS)
		
		executeCommands(settings.enabled, settings.commands)
	}
	
	fun runPartyCommands()
	{
		val settings = party.conf().getProperty(PartySettings.PARTY_COMMANDS)
		
		executeCommands(settings.enabled, settings.commands)
	}
	
	fun runPartyStartEffects()
	{
		val settings = party.conf().getProperty(EffectsSettings.PARTY_START)
		
		executeEffects(settings.enable, settings.effects, server.onlinePlayers,
		               settings.offsetX, settings.offsetY, settings.offsetZ,
		               settings.speed, settings.count)
	}
	
	fun runPartyCommandEffects(player: Player)
	{
		val settings = party.conf().getProperty(EffectsSettings.PARTY_COMMAND_EXECUTE)
		
		executeEffects(settings.enable, settings.effects, listOf(player),
		               settings.offsetX, settings.offsetY, settings.offsetZ,
		               settings.speed, settings.count)
	}
	
	
	fun buildCrate(amount: Int): ItemStack
	{
		val item = party.conf().getProperty(CrateSettings.MATERIAL).parseItem(true) ?: ItemStack(Material.CHEST, 1)
		item.amount = amount
		
		return item.meta()
		{
			name = party.conf().getProperty(CrateSettings.NAME) ?: "Vote Party Crate"
			lore = party.conf().getProperty(CrateSettings.LORE).map(::color)
		}
	}
	
	fun startParty()
	{
		runPrePartyCommands()
		
		plugin.runTaskLater(party.conf().getProperty(PartySettings.START_DELAY) * 20L)
		{
			runPartyCommands()
			runPartyStartEffects()
			
			server.onlinePlayers.forEach()
			{
				giveGuaranteedPartyRewards(it)
				giveRandomPartyRewards(it)
			}
		}
	}
	
	
	private fun executeCommands(enabled: Boolean, cmds: Collection<String>?, player: Player? = null)
	{
		if (!enabled || (player != null && (player.world.name in party.conf().getProperty(PartySettings.DISABLED_WORLDS))))
		{
			return
		}
		
		cmds?.forEach()
		{
			server.dispatchCommand(server.consoleSender, if (player == null) it else formMessage(player, it))
		}
	}
	
	
	private fun executeEffects(enabled: Boolean, effects: Collection<String>, players: Collection<Player>, offsetX: Double, offsetY: Double, offsetZ: Double, speed: Double, count: Int)
	{
		if (!enabled)
		{
			return
		}
		
		val targets = players.filter()
		{
			it.world.name !in party.conf().getProperty(PartySettings.DISABLED_WORLDS)
		}
		
		effects.forEach()
		{ effect ->
			targets.forEach()
			{ player ->
				party.hook().display(EffectType.valueOf(effect), player.location, offsetX, offsetY, offsetZ, speed, count)
			}
		}
	}
	
}