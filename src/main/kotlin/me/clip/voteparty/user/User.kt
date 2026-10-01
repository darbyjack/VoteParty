package me.clip.voteparty.user

import me.clip.voteparty.conf.objects.CumulativeVoteCommands
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.util.UUID

/**
 * A player's votes, and how far into them the cumulative rewards have been handed over.
 *
 * [settled] is null for a player file written before VoteParty kept track of that, which is the
 * only way to tell such a file apart from a fresh one — everything they hold predates the
 * tracking, so it all counts as settled.
 */
data class User(val uuid: UUID, var name: String, private val data: MutableList<Long>, var claimable: Int, private var settled: Int? = 0)
{
	
	fun voted()
	{
		voted(System.currentTimeMillis())
	}
	
	/**
	 * Records a vote at a given time. [voted] is the entry point everything else uses; this takes
	 * the time so a vote can be placed in a period without waiting for one to come round.
	 */
	internal fun voted(epoch: Long)
	{
		data += epoch
	}
	
	fun votes(): List<Long>
	{
		return data
	}
	
	fun hasVotedBefore(): Boolean
	{
		return data.isNotEmpty()
	}
	
	fun reset()
	{
		data.clear()
		settled = 0
	}
	
	fun player() : OfflinePlayer
	{
		return Bukkit.getOfflinePlayer(uuid)
	}
	
	/**
	 * The cumulative reward entries this player has reached since [epoch] but has not been paid
	 * for yet.
	 *
	 * A cumulative reward is keyed to a vote count rather than to a vote, so each threshold only
	 * ever comes due once. Comparing the count against the votes already settled — rather than
	 * against the threshold exactly — is what makes it reachable at all: a vote that could not be
	 * paid out where it was cast (offline, or an inventory too full to receive anything) never got
	 * as far as a check, and by the time there is one the count has stepped over the threshold.
	 * Everything between the settled count and the count now is therefore still owed, which also
	 * covers several thresholds being crossed at once.
	 *
	 * Nothing is settled here. The caller settles only once the rewards have really been handed
	 * over, so a threshold that could not be paid stays owed rather than being lost.
	 *
	 * A period that rolls over needs no resetting: the settled votes fall out of the new period's
	 * window along with everything else, so its thresholds start coming due again.
	 */
	internal fun dueCumulativeRewards(epoch: Long, entries: List<CumulativeVoteCommands>): List<CumulativeVoteCommands>
	{
		val settledCount = settledVotes().count { it >= epoch }
		val count = data.count { it >= epoch }
		
		return entries.filter { it.votes > settledCount && it.votes <= count }
	}
	
	/**
	 * The votes that have already been handed the cumulative rewards they reached.
	 *
	 * [data] only ever grows by appending, so counting how far into it the payout has got is
	 * enough to tell which thresholds are still owed, and stays right across a restart.
	 */
	private fun settledVotes(): List<Long>
	{
		val reached = settled ?: data.size
		
		return data.subList(0, reached.coerceIn(0, data.size))
	}
	
	/**
	 * Records that every vote cast so far has been handed the cumulative rewards it reached, so
	 * none of those thresholds are paid out a second time.
	 */
	internal fun settleVotes()
	{
		settled = data.size
	}
	
}