package me.clip.voteparty.base

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
	
}