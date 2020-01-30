package me.clip.voteparty.handler

import me.clip.voteparty.base.Addon
import me.clip.voteparty.base.State
import me.clip.voteparty.conf.sections.EffectsSettings
import me.clip.voteparty.conf.sections.PartySettings
import me.clip.voteparty.conf.sections.PluginSettings
import me.clip.voteparty.conf.sections.VoteSettings
import me.clip.voteparty.exte.formMessage
import me.clip.voteparty.exte.takeRandomly
import me.clip.voteparty.plugin.VotePartyPlugin
import me.clip.voteparty.version.EffectType
import org.bukkit.entity.Player
import java.util.concurrent.atomic.AtomicInteger

class VotesHandler(override val plugin: VotePartyPlugin) : Addon, State
{
	
	private val votes = AtomicInteger()
	
	
	override fun load()
	{
		votes.set(party.conf().getProperty(PluginSettings.COUNTER))
	}
	
	override fun kill()
	{
		votes.set(0)
	}
	
	
	fun getVotes(): Int
	{
		return votes.get()
	}
	
	fun setVotes(amount: Int)
	{
		votes.set(amount)
	}
	
	fun addVotes(amount: Int)
	{
		if (votes.addAndGet(amount) < party.conf().getProperty(PartySettings.VOTES_NEEDED))
		{
			return
		}
		
		votes.set(0)
		party.partyHandler.startParty()
	}
	
	
	fun giveGuaranteedVoteRewards(player: Player)
	{
		val settings = party.conf().getProperty(VoteSettings.GUARANTEED_REWARDS)
		
		if (!settings.enabled || settings.commands.isEmpty())
		{
			return
		}
		
		settings.commands.forEach()
		{ command ->
			server.dispatchCommand(server.consoleSender, formMessage(player, command))
		}
	}
	
	fun givePermissionVoteRewards(player: Player)
	{
		val settings = party.conf().getProperty(VoteSettings.PERMISSION_VOTE_REWARDS)
		
		if (!settings.enabled || settings.permCommands.isEmpty())
		{
			return
		}
		
		settings.permCommands.filter { player.hasPermission(it.permission) }.forEach()
		{ perm ->
			perm.commands.forEach()
			{ command ->
				server.dispatchCommand(server.consoleSender, formMessage(player, command))
			}
		}
	}
	
	fun giveRandomVoteRewards(player: Player)
	{
		val settings = party.conf().getProperty(VoteSettings.PER_VOTE_REWARDS)
		
		if (!settings.enabled || settings.max_possible <= 0 || settings.commands.isEmpty())
		{
			return
		}
		
		settings.commands.takeRandomly(settings.max_possible).forEach()
		{
			it.command.forEach()
			{
				server.dispatchCommand(server.consoleSender, formMessage(player, it))
			}
		}
	}
	
	fun playerVoteEffects(player: Player)
	{
		val settings = party.conf().getProperty(EffectsSettings.VOTE)
		
		if (!settings.enable || settings.effects.isEmpty())
		{
			return
		}
		
		val location = player.location
		
		settings.effects.forEach {
			party.hook().display(EffectType.valueOf(it), location, settings.offsetX, settings.offsetY, settings.offsetZ, settings.speed, settings.count)
		}
	}
	
	fun runGlobalCommands(player: Player)
	{
		val settings = party.conf().getProperty(VoteSettings.GLOBAL_COMMANDS)
		
		if (!settings.enabled || settings.commands.isEmpty())
		{
			return
		}
		
		settings.commands.forEach()
		{ command ->
			server.dispatchCommand(server.consoleSender, formMessage(player, command))
		}
	}
	
}