package me.clip.voteparty.handler

import me.clip.voteparty.base.Addon
import me.clip.voteparty.base.formMessage
import me.clip.voteparty.base.reduce
import me.clip.voteparty.conf.ConfigVoteParty
import me.clip.voteparty.plugin.VotePartyPlugin
import org.bukkit.entity.Player
import java.util.concurrent.atomic.AtomicInteger

class VotesHandler(override val plugin: VotePartyPlugin) : Addon
{
	
	private val conf: ConfigVoteParty
		get() = party.conf()
	
	val votes = AtomicInteger()
	
	fun addVote(amount: Int)
	{
		if (votes.addAndGet(amount) < conf.party?.votes_needed ?: 50)
		{
			return
		}
		
		votes.set(0)
		party.partyHandler.startParty()
	}
	
	fun giveGuaranteedVoteRewards(player: Player)
	{
		if (conf.voting?.guaranteed_rewards?.enabled == false)
		{
			return
		}
		
		val cmds = conf.voting?.guaranteed_rewards?.commands ?: return
		cmds.forEach()
		{ command ->
			server.dispatchCommand(server.consoleSender, formMessage(player, command))
		}
	}
	
	fun giveRandomVoteRewards(player: Player)
	{
		if (conf.voting?.per_vote_rewards?.enabled == false)
		{
			return
		}
		
		val take = conf.voting?.per_vote_rewards?.max_possible?.takeIf { it > 0 } ?: return
		val cmds = conf.voting?.per_vote_rewards?.commands?.takeIf { it.isNotEmpty() } ?: return
		
		cmds.reduce(take).forEach()
		{
			server.dispatchCommand(server.consoleSender, formMessage(player, it.command))
		}
	}
	
	fun playerVoteEffects(player: Player)
	{
		if (conf.effects?.vote?.enabled == false)
		{
			return
		}
		
		val effects = conf.effects?.vote?.effects?.filterNotNull()?.takeIf { it.isNotEmpty() } ?: return
		
		val loc = player.location
		
		effects.forEach {
			party.hook().display(it, loc, null)
		}
	}
	
	fun runGlobalCommands(player: Player)
	{
		if (conf.voting?.global_commands?.enabled == false)
		{
			return
		}
		
		val cmds = conf.voting?.global_commands?.commands ?: return
		cmds.forEach()
		{ command ->
			server.dispatchCommand(server.consoleSender, formMessage(player, command))
		}
	}
	
}