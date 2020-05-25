package me.clip.voteparty.placeholders

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import me.clip.voteparty.VoteParty
import me.clip.voteparty.leaderboard.LeaderboardType
import org.bukkit.OfflinePlayer

class VotePartyPlaceholders(private val voteParty: VoteParty) : PlaceholderExpansion()
{
	
	override fun getAuthor(): String
	{
		return "Glare"
	}
	
	override fun getVersion(): String
	{
		return "2.0"
	}
	
	override fun getIdentifier(): String
	{
		return "voteparty"
	}
	
	override fun persist(): Boolean
	{
		return true
	}
	
	
	override fun onRequest(offlinePlayer: OfflinePlayer, arg: String): String
	{

		if (arg.startsWith("top_")) {
			return getTop(arg.replace("top_", "").toLowerCase())
		}

		if (arg.startsWith("placement_")) {
			return getPlacement(arg.replace("placement_", "").toLowerCase(), offlinePlayer)
		}
		
		return when (arg.toLowerCase())
		{
			"votes_recorded"       -> voteParty.getVotes().toString()
			"votes_required_party" -> voteParty.getVotesNeeded().minus(voteParty.getVotes()).toString()
			"votes_required_total" -> voteParty.getVotesNeeded().toString()
			"player_votes"         -> voteParty.getPlayerVotes(offlinePlayer).toString()
			else                   -> ""
		}
	}
	
	/**
	 * Get the "top" data for a leaderboard
	 */
	private fun getTop(input: String): String
	{
		// Split it by the _
		val split = input.split('_')
		
		// Return empty if not enough data given
		if (split.size < 3) {
			return ""
		}
		
		// Get the leaderboard type but return empty if invalid type
		val leaderboard = voteParty.leaderboardHandler.getLeaderboard(LeaderboardType.valueOf(split[0].toUpperCase())) ?: return ""
		// Get the info type
		val type = split[1]
		// Get the placeholder
		val index = split[2]
		// Get the user if the placeholder isn't null
		val user = leaderboard.getEntry(Integer.parseInt(index)) ?: return ""
		// Return the correct data
		return if (type == "name") user.name() else user.votes.toString()
	}

	/**
	 * Get the placement of a player based on the given leaderboard type
	 */
	private fun getPlacement(input: String, offlinePlayer: OfflinePlayer) : String
	{
		val leaderboard = voteParty.leaderboardHandler.getLeaderboard(LeaderboardType.valueOf(input.toUpperCase())) ?: return ""
		val user = voteParty.usersHandler[offlinePlayer]
		val placement = leaderboard.getPlacement(user) ?: return ""
		return placement.plus(1).toString()
	}
	
}