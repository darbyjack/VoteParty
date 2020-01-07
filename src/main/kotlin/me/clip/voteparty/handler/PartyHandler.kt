package me.clip.voteparty.handler

import ch.jalu.configme.SettingsManager
import me.clip.voteparty.base.Addon
import me.clip.voteparty.base.color
import me.clip.voteparty.base.formMessage
import me.clip.voteparty.base.meta
import me.clip.voteparty.base.name
import me.clip.voteparty.base.reduce
import me.clip.voteparty.base.runTaskLater
import me.clip.voteparty.base.runTaskTimer
import me.clip.voteparty.config.sections.CrateSettings
import me.clip.voteparty.config.sections.EffectsSettings
import me.clip.voteparty.config.sections.PartySettings
import me.clip.voteparty.plugin.VotePartyPlugin
import me.clip.voteparty.version.EffectType
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class PartyHandler(override val plugin: VotePartyPlugin) : Addon
{
	private val conf: SettingsManager
		get() = party.conf()
	
	val crate: ItemStack
		get() = buildCrate(1)
	
	
	fun giveRandomPartyRewards(player: Player)
	{
		val take = conf.getProperty(PartySettings.REWARD_COMMANDS).max_possible?.takeIf { it > 0 } ?: return
		val cmds = conf.getProperty(PartySettings.REWARD_COMMANDS).commands.takeIf { it.isNotEmpty() } ?: return
		
		val iter = cmds.reduce(take).iterator()
		plugin.runTaskTimer(conf.getProperty(PartySettings.COMMAND_DELAY).toLong() * 20)
		{
			if (!iter.hasNext())
			{
				return@runTaskTimer cancel()
			}
			
			runPartyCommandEffects(player)
			server.dispatchCommand(server.consoleSender, formMessage(player, iter.next().command))
		}
	}
	
	fun giveGuaranteedPartyRewards(player: Player)
	{
		executeCommands(conf.getProperty(PartySettings.GUARANTEED_REWARDS).enabled,
		                conf.getProperty(PartySettings.GUARANTEED_REWARDS).commands,
		                player)
	}
	
	fun runPrePartyCommands()
	{
		executeCommands(conf.getProperty(PartySettings.PRE_PARTY_COMMANDS).enabled,
		                conf.getProperty(PartySettings.PRE_PARTY_COMMANDS).commands)
	}
	
	fun runPartyCommands()
	{
		executeCommands(conf.getProperty(PartySettings.PARTY_COMMANDS).enabled,
		                conf.getProperty(PartySettings.PARTY_COMMANDS).commands)
	}
	
	fun runPartyStartEffects()
	{
		executeEffects(conf.getProperty(EffectsSettings.PARTY_START).enable,
		               conf.getProperty(EffectsSettings.PARTY_START).effects,
		               server.onlinePlayers)
	}
	
	fun runPartyCommandEffects(player: Player)
	{
		executeEffects(conf.getProperty(EffectsSettings.PARTY_COMMAND_EXECUTE).enable,
		               conf.getProperty(EffectsSettings.PARTY_COMMAND_EXECUTE).effects,
		               listOf(player))
	}
	
	
	fun buildCrate(amount: Int): ItemStack
	{
		val item = conf.getProperty(CrateSettings.MATERIAL).parseItem(true) ?: ItemStack(Material.CHEST, 1)
		item.amount = amount
		
		return item.meta()
		{
			name = conf.getProperty(CrateSettings.NAME) ?: "Vote Party Crate"
			lore = conf.getProperty(CrateSettings.LORE).map(::color)
		}
	}
	
	fun startParty()
	{
		runPrePartyCommands()
		
		plugin.runTaskLater(conf.getProperty(PartySettings.START_DELAY) * 20L)
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
	
	
	private fun executeCommands(enabled: Boolean?, cmds: Collection<String>?, player: Player? = null)
	{
		if (enabled == false)
		{
			return
		}
		
		if (player != null && (player.world.name in party.conf().getProperty(PartySettings.DISABLED_WORLDS)))
		{
			return
		}
		
		cmds?.forEach()
		{
			server.dispatchCommand(server.consoleSender, if (player == null) it else formMessage(player, it))
		}
	}
	
	
	private fun executeEffects(enabled: Boolean?, effects: Collection<String>, players: Collection<Player>)
	{
		if (enabled == false)
		{
			return
		}
		
		val targets = players.filter()
		{
			it.world.name !in conf.getProperty(PartySettings.DISABLED_WORLDS)
		}
		
		effects.forEach()
		{ effect ->
			targets.forEach()
			{ player ->
				party.hook().display(EffectType.valueOf(effect), player.location, null)
			}
		}
	}
	
}