package me.clip.voteparty.listener

import me.clip.voteparty.plugin.VotePartyListener
import me.clip.voteparty.plugin.VotePartyPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class CrateListener(override val plugin: VotePartyPlugin) : VotePartyListener
{
	
	@EventHandler
	fun PlayerInteractEvent.onInteract()
	{
		if (plugin.voteParty?.conf()?.crate?.enabled == false)
		{
			return
		}
		
		if ((action != Action.RIGHT_CLICK_AIR) && (action != Action.RIGHT_CLICK_BLOCK))
		{
			return
		}
		
		val hand = player.inventory.itemInHand
		val crate = plugin.voteParty?.partyHandler?.crate
		
		if (!hand.isSimilar(crate))
		{
			return
		}
		
		if (player.world in plugin.voteParty?.conf()?.party?.disabled_worlds ?: emptySet())
		{
			return
		}
		
		isCancelled = true
		
		if (hand.amount == 1)
		{
			player.inventory.removeItem(hand)
		}
		else
		{
			player.inventory.itemInHand.amount = hand.amount - 1
		}
		
		plugin.voteParty?.partyHandler?.giveGuaranteedPartyRewards(player)
		plugin.voteParty?.partyHandler?.giveRandomPartyRewards(player)
	}
}