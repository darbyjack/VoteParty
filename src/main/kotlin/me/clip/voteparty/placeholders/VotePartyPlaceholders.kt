package me.clip.voteparty.placeholders

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player

class VotePartyPlaceholders : PlaceholderExpansion()
{
	override fun getIdentifier(): String
	{
		return "voteparty"
	}
	
	override fun persist(): Boolean
	{
		return true
	}
	
	override fun getAuthor(): String
	{
		return "Glare"
	}
	
	override fun getVersion(): String
	{
		return "2.0"
	}
	
	override fun onPlaceholderRequest(player: Player, arg: String): String
	{
		
		return when (arg.toLowerCase())
		{
			"counter" -> "125"
			else -> ""
		}
	}
}