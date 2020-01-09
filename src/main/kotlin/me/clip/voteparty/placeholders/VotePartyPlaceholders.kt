package me.clip.voteparty.placeholders

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import me.clip.voteparty.VoteParty
import org.bukkit.OfflinePlayer

class VotePartyPlaceholders(private val voteParty: VoteParty) : PlaceholderExpansion()
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
	
	override fun onRequest(offlinePlayer: OfflinePlayer, arg: String): String
	{
		
		return when (arg.toLowerCase())
		{
			"votes_recorded"       -> voteParty.getVotes().toString()
			"votes_required_party" -> voteParty.getVotesNeeded().minus(voteParty.getVotes()).toString()
			"votes_required_total" -> voteParty.getVotesNeeded().toString()
			"player_votes" -> voteParty.votePlayerHandler[offlinePlayer]?.data?.size.toString()
			else                   -> ""
		}
	}
}