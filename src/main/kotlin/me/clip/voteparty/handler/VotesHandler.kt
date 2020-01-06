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
		
		if (!conf.getProperty(VoteSettings.GUARANTEED_REWARDS).enabled)
		{
			return
		}
		
		val cmds = conf.getProperty(VoteSettings.GUARANTEED_REWARDS).commands
		cmds.forEach()
		{ command ->
			server.dispatchCommand(server.consoleSender, formMessage(player, command))
		}
	}
	
	fun giveRandomVoteRewards(player: Player)
	{
		if (!conf.getProperty(VoteSettings.PER_VOTE_REWARDS).enabled)
		{
			return
		}
		val take = conf.getProperty(VoteSettings.PER_VOTE_REWARDS).max_possible.takeIf { it > 0 } ?: return
		val cmds = conf.getProperty(VoteSettings.PER_VOTE_REWARDS).commands.takeIf { it.isNotEmpty() } ?: return
		
		cmds.reduce(take).forEach()
		{
			server.dispatchCommand(server.consoleSender, formMessage(player, it.command))
		}
	}
	
	fun playerVoteEffects(player: Player)
	{
		if (!conf.getProperty(EffectsSettings.VOTE).enable)
		{
			return
		}
		val effects = conf.getProperty(EffectsSettings.VOTE).effects.takeIf { it.isNotEmpty() } ?: return
		
		val loc = player.location
		
		effects.forEach {
			party.hook().display(EffectType.valueOf(it), loc, null)
		}
	}
	
	fun runGlobalCommands(player: Player)
	{
		if (!conf.getProperty(VoteSettings.GLOBAL_COMMANDS).enabled)
		{
			return
		}
		
		val cmds = conf.getProperty(VoteSettings.GLOBAL_COMMANDS).commands
		cmds.forEach()
		{ command ->
			server.dispatchCommand(server.consoleSender, formMessage(player, command))
		}
	}
	
}