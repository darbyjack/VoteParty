package me.clip.voteparty.listener

import com.vexsoftware.votifier.model.VotifierEvent
import me.clip.voteparty.events.VoteReceivedEvent
import me.clip.voteparty.plugin.VotePartyListener
import me.clip.voteparty.plugin.VotePartyPlugin
import org.bukkit.event.EventHandler

class NuVotifierListener(override val plugin: VotePartyPlugin) : VotePartyListener
{
	
	@EventHandler
	fun VotifierEvent.onVote()
	{
		val player = server.getOfflinePlayer(vote.username)
		
		val event = VoteReceivedEvent(player)
		server.pluginManager.callEvent(event)
	}
}