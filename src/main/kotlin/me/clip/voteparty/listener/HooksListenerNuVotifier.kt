package me.clip.voteparty.listener

import com.vexsoftware.votifier.model.VotifierEvent
import me.clip.voteparty.events.VoteReceivedEvent
import me.clip.voteparty.listener.base.VotePartyListener
import me.clip.voteparty.plugin.VotePartyPlugin
import org.bukkit.event.EventHandler

internal class HooksListenerNuVotifier(override val plugin: VotePartyPlugin) : VotePartyListener
{

	@EventHandler
	fun VotifierEvent.onVote()
	{
		if (vote == null || vote.username.isNullOrEmpty()) {
			plugin.logger.warning("A vote come through NuVotifier which was null or did not provide a username. Throwing away.")
			return
		}

		// Try pulling the username from the user cache first before querying Bukkit / Mojang
		val player = party.usersHandler[vote.username]?.player() ?: server.getOfflinePlayer(vote.username)
		val event = VoteReceivedEvent(player, vote.serviceName)
		server.pluginManager.callEvent(event)
	}

}
