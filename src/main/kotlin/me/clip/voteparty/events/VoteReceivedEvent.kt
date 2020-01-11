package me.clip.voteparty.events

import org.bukkit.OfflinePlayer
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class VoteReceivedEvent(val player: OfflinePlayer) : Event(), Cancellable
{
	
	private var isCancelled = false
	
	
	override fun isCancelled(): Boolean
	{
		return isCancelled
	}
	
	override fun setCancelled(cancelled: Boolean)
	{
		isCancelled = cancelled
	}
	
	
	override fun getHandlers(): HandlerList
	{
		return handlerList
	}
	
	
	companion object
	{
		@JvmStatic
		val handlerList = HandlerList()
	}
	
}
