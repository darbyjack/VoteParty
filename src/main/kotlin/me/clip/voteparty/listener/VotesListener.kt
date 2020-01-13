package me.clip.voteparty.listener

import me.clip.voteparty.conf.sections.PartySettings
import me.clip.voteparty.conf.sections.VoteSettings
import me.clip.voteparty.events.VoteReceivedEvent
import me.clip.voteparty.exte.sendMessage
import me.clip.voteparty.listener.base.VotePartyListener
import me.clip.voteparty.messages.Messages
import me.clip.voteparty.plugin.VotePartyPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent

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

	@EventHandler
	fun PlayerJoinEvent.onJoin()
	{
		if (!player.hasPlayedBefore())
		{
			return
		}

		val user = party.usersHandler[player]
		val claimable = user?.claimable ?: 0
		if (claimable > 0 && party.conf().getProperty(VoteSettings.OFFLINE_VOTE_CLAIMING_NOTIFY))
		{
			server.scheduler.runTaskLater(plugin, Runnable {
				sendMessage(party.manager().getCommandIssuer(player), Messages.CLAIM__NOTIFY)
			}, 40L)
		}
	}
	
}