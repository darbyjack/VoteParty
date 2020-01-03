package me.clip.voteparty.base

import me.clip.voteparty.VoteParty
import me.clip.voteparty.plugin.VotePartyPlugin
import org.bukkit.Server

/**
 * Defines an aspect of the plugin that requires access to spigot api
 */
interface Addon
{
	
	val plugin: VotePartyPlugin
	
	
	val server: Server
		get() = plugin.server
	
	
	val party: VoteParty
		get() = checkNotNull(plugin.voteParty)
		{
			"vote party is unavailable"
		}
	
	val prefix: String
		get() = party.conf().settings?.prefix ?: PREFIX
	
}