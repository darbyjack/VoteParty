package me.clip.voteparty.handler

import ch.jalu.configme.SettingsManager
import me.clip.voteparty.base.Addon
import me.clip.voteparty.base.formMessage
import me.clip.voteparty.base.reduce
import me.clip.voteparty.config.sections.EffectsSettings
import me.clip.voteparty.config.sections.PartySettings
import me.clip.voteparty.config.sections.VoteSettings
import me.clip.voteparty.plugin.VotePartyPlugin
import me.clip.voteparty.version.EffectType
import org.bukkit.entity.Player
import java.util.concurrent.atomic.AtomicInteger

class VotesHandler(override val plugin: VotePartyPlugin) : Addon
{
	
	private val conf: SettingsManager
		get() = party.conf()
	
	val votes = AtomicInteger()
	
	fun addVote(amount: Int)
	{
		
		if (votes.addAndGet(amount) < conf.getProperty(PartySettings.VOTES_NEEDED))
		{
			return
		}
		
		votes.set(0)
		party.partyHandler.startParty()
	}
	
	fun giveGuaranteedVoteRewards(player: Player)
	{
		val settings = conf.getProperty(VoteSettings.GUARANTEED_REWARDS)
		
		if (!settings.enabled)
		{
			return
		}
		
		val cmds = settings.commands
		cmds.forEach()
		{ command ->
			server.dispatchCommand(server.consoleSender, formMessage(player, command))
		}
	}
	
	fun giveRandomVoteRewards(player: Player)
	{
		val settings = conf.getProperty(VoteSettings.PER_VOTE_REWARDS)
		
		if (!settings.enabled)
		{
			return
		}
		val take = settings.max_possible.takeIf { it > 0 } ?: return
		val cmds = settings.commands.takeIf { it.isNotEmpty() } ?: return
		
		cmds.reduce(take).forEach()
		{
			server.dispatchCommand(server.consoleSender, formMessage(player, it.command))
		}
	}
	
	fun playerVoteEffects(player: Player)
	{
		val settings = conf.getProperty(EffectsSettings.VOTE)
		
		if (!settings.enable)
		{
			return
		}
		val effects = settings.effects.takeIf { it.isNotEmpty() } ?: return
		
		val loc = player.location
		
		effects.forEach {
			party.hook().display(EffectType.valueOf(it), loc, settings.offsetX, settings.offsetY, settings.offsetZ, settings.speed, settings.count)
		}
	}
	
	fun runGlobalCommands(player: Player)
	{
		val settings = conf.getProperty(VoteSettings.GLOBAL_COMMANDS)
		
		if (!settings.enabled)
		{
			return
		}
		
		val cmds = settings.commands
		cmds.forEach()
		{ command ->
			server.dispatchCommand(server.consoleSender, formMessage(player, command))
		}
	}
	
}