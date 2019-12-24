package me.clip.voteparty.plugin.base

import me.clip.voteparty.base.Addon
import me.clip.voteparty.base.State
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

interface VotePartyListener : Addon, State, Listener
{
	
	override fun load()
	{
		server.pluginManager.registerEvents(this, plugin)
	}
	
	override fun kill()
	{
		HandlerList.unregisterAll(this)
	}
	
}