package me.clip.voteparty.listener

import me.clip.voteparty.conf.sections.CrateSettings
import me.clip.voteparty.conf.sections.PartySettings
import me.clip.voteparty.listener.base.VotePartyListener
import me.clip.voteparty.plugin.VotePartyPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

internal class CrateListener(override val plugin: VotePartyPlugin) : VotePartyListener
{
	
	@EventHandler
	fun PlayerInteractEvent.onInteract()
	{
		if (party.conf().getProperty(CrateSettings.ENABLED) == false)
		{
			return
		}
		
		if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK)
		{
			return
		}
		
		val held = player.inventory.itemInHand
		val item = party.partyHandler.buildCrate(1)
		
		if (!held.isSimilar(item))
		{
			return
		}
		
		if (player.world.name in party.conf().getProperty(PartySettings.DISABLED_WORLDS))
		{
			return
		}
		
		isCancelled = true
		
		if (held.amount == 1)
		{
			player.inventory.removeItem(held)
		}
		else
		{
			player.inventory.itemInHand.amount = held.amount - 1
		}
		
		party.partyHandler.runAll(player)
	}
	
}