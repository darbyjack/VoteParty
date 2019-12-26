package me.clip.voteparty.listeners

import com.vexsoftware.votifier.model.VotifierEvent
import me.clip.voteparty.plugin.VotePartyListener
import me.clip.voteparty.plugin.VotePartyPlugin
import org.bukkit.event.EventHandler

class VoteListener(override val plugin: VotePartyPlugin) : VotePartyListener
{
	
	@EventHandler
	fun VotifierEvent.onVote()
	{
		val player = server.getPlayer(vote.username)
		
		if (player != null)
		{
			// handle guaranteed, random, and effects
		}
		
		// handle vote
	}
	
}