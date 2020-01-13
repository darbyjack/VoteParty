package me.clip.voteparty.listener

import me.clip.voteparty.conf.sections.PartySettings
import me.clip.voteparty.conf.sections.VoteSettings
import me.clip.voteparty.events.VoteReceivedEvent
import me.clip.voteparty.listener.base.VotePartyListener
import me.clip.voteparty.plugin.VotePartyPlugin
import org.bukkit.event.EventHandler

internal class VotesListener(override val plugin: VotePartyPlugin) : VotePartyListener
{
	
	@EventHandler
	fun VoteReceivedEvent.onReceive()
	{
		if (!player.isOnline && !party.conf().getProperty(PartySettings.OFFLINE_VOTES))
		{
			return
		}

		val user = party.usersHandler[player]

		if (party.conf().getProperty(VoteSettings.OFFLINE_VOTE_CLAIMING))
		{
			user?.inc()
		}
		
		party.votesHandler.addVotes(1)
		party.usersHandler[player]?.data?.add(System.currentTimeMillis())
		
		val online = player.player ?: return
		
		party.votesHandler.runGlobalCommands(online)
		party.votesHandler.giveGuaranteedVoteRewards(online)
		party.votesHandler.giveRandomVoteRewards(online)
		party.votesHandler.playerVoteEffects(online)
	}
	
}