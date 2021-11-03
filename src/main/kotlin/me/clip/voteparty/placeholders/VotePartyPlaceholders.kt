package me.clip.voteparty.placeholders

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import me.clip.voteparty.VoteParty
import me.clip.voteparty.leaderboard.LeaderboardType
import org.bukkit.OfflinePlayer
import java.util.*

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


	override fun onRequest(player: OfflinePlayer?, arg: String): String {
		if (arg.startsWith("top_")) {
			return getTop(arg.replace("top_", "").lowercase(Locale.getDefault()))
		}

		if (arg.startsWith("placement_")) {
			return getPlacement(arg.replace("placement_", "").lowercase(Locale.getDefault()), player ?: return "")
		}

		if (arg.startsWith("totalvotes_")) {
			return getVotes(arg.replace("totalvotes_", "").lowercase(Locale.getDefault()), player ?: return "")
		}

		return when (arg.lowercase(Locale.getDefault())) {
			"votes_recorded" -> voteParty.getVotes().toString()
			"votes_required_party" -> voteParty.getVotesNeeded().minus(voteParty.getVotes()).toString()
			"votes_required_total" -> voteParty.getVotesNeeded().toString()
			"votes_total" -> voteParty.usersHandler.getTotalVotes().toString()
			"player_votes" -> voteParty.usersHandler[player ?: return ""].votes().size.toString()
			else -> ""
		}
	}

	/**
	 * Get the "top" data for a leaderboard
	 */
	private fun getTop(input: String): String
	{
		val (type, info, placement) = input.split('_').takeIf { it.size >= 3 } ?: return ""

		// Get the leaderboard type but return empty if invalid type
		val leaderboard = LeaderboardType.find(type)?.let(voteParty.leaderboardHandler::getLeaderboard) ?: return ""

		// Get the user if the placeholder isn't null
		val user = placement.toIntOrNull()?.let(leaderboard::getEntry) ?: return ""

		// Return the correct data
		return when (info)
		{
			"name"  -> user.name()
			"votes" -> user.votes.toString()
			else    -> ""
		}
	}

	/**
	 * Get the placement of a player based on the given leaderboard type
	 */
	private fun getPlacement(input: String, player: OfflinePlayer): String
	{
		val leaderboard = LeaderboardType.find(input)?.let(voteParty.leaderboardHandler::getLeaderboard) ?: return ""

		return leaderboard.getPlacement(voteParty.usersHandler[player])?.plus(1)?.toString() ?: ""
	}

	/**
	 * Get the amount of votes a player has based on the given leaderboard type
	 */
	private fun getVotes(input: String, player: OfflinePlayer): String
	{
		return LeaderboardType.find(input)?.let { voteParty.usersHandler.getVotesSince(player, it.time.invoke()) }?.toString() ?: ""
	}

}
