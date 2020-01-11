package me.clip.voteparty.listener.base

import me.clip.voteparty.base.Addon
import me.clip.voteparty.base.State
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

internal interface VotePartyListener : Addon, State, Listener
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