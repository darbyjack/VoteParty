package me.clip.voteparty.base

import me.clip.voteparty.VoteParty
import me.clip.voteparty.config.sections.PluginSettings
import me.clip.voteparty.plugin.VotePartyPlugin
import org.bukkit.Server
import java.util.logging.Logger

/**
 * Defines an aspect of the plugin that requires access to spigot api
 */
interface Addon
{
	
	val plugin: VotePartyPlugin
	
	
	val server: Server
		get() = plugin.server
	
	val logger: Logger
		get() = plugin.logger
	
	
	val party: VoteParty
		get() = checkNotNull(plugin.voteParty)
		{
			"vote party is unavailable"
		}
	
	val prefix: String
		get() = party.conf().getProperty(PluginSettings.PREFIX) ?: PREFIX
	
}