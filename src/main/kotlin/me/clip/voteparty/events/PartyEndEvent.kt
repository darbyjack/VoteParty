package me.clip.voteparty.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PartyEndEvent : Event()
{
	
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