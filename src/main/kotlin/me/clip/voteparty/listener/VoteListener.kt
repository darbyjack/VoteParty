package me.clip.voteparty.listener

import com.vexsoftware.votifier.model.VotifierEvent
import me.clip.voteparty.config.sections.PartySettings
import me.clip.voteparty.plugin.VotePartyListener
import me.clip.voteparty.plugin.VotePartyPlugin
import org.bukkit.event.EventHandler

class VoteListener(override val plugin: VotePartyPlugin) : VotePartyListener
{
	
	@EventHandler
	fun VotifierEvent.onVote()
	{
		
		val player = server.getOfflinePlayer(vote.username)
		
		if (!player.isOnline && party.conf().getProperty(PartySettings.OFFLINE_VOTES) == true)
		{
			plugin.voteParty?.votesHandler?.addVote(1)
			return
		}
		
		plugin.voteParty?.votesHandler?.addVote(1)
		
		val online = player.player ?: return
		
		party.votesHandler.runGlobalCommands(online)
		party.votesHandler.giveGuaranteedVoteRewards(online)
		party.votesHandler.giveRandomVoteRewards(online)
		party.votesHandler.playerVoteEffects(online)
	}
}