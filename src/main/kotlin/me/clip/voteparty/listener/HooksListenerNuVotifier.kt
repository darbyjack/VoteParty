package me.clip.voteparty.listener

import com.vexsoftware.votifier.model.VotifierEvent
import me.clip.voteparty.conf.sections.VoteSettings
import me.clip.voteparty.events.VoteReceivedEvent
import me.clip.voteparty.listener.base.VotePartyListener
import me.clip.voteparty.plugin.VotePartyPlugin
import org.bukkit.event.EventHandler
import java.util.regex.Pattern

internal class HooksListenerNuVotifier(override val plugin: VotePartyPlugin) : VotePartyListener
{
	private lateinit var usernameRegex: Pattern
	override fun load() {
		super.load()
		usernameRegex = Pattern.compile(party.conf().getProperty(VoteSettings.NAME_REGEX))
	}

	@EventHandler
	fun VotifierEvent.onVote()
	{
		if (vote == null || vote.username.isNullOrEmpty()) {
			plugin.logger.warning("A vote come through NuVotifier which was null or did not provide a username. Throwing away.")
			return
		}

		if (party.conf().getProperty(VoteSettings.VALIDATE_NAMES) && !usernameRegex.matcher(vote.username).matches()) {
			plugin.logger.warning("A vote came through NuVotifier (username: ${vote.username}) which did not match the username regex. Throwing away.")
			return
		}

		// Try pulling the username from the user cache first before querying Bukkit / Mojang
		val player = party.usersHandler[vote.username]?.player() ?: server.getOfflinePlayer(vote.username)
		val event = VoteReceivedEvent(player, vote.serviceName)
		server.pluginManager.callEvent(event)
	}

}
