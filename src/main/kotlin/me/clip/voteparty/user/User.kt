package me.clip.voteparty.user

import me.clip.voteparty.conf.objects.CumulativeVoteCommands
import me.clip.voteparty.leaderboard.LeaderboardType
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.util.UUID

/**
 * A cumulative reward threshold a player has reached and has not been handed over yet.
 *
 * [periodStart] is what makes a threshold in one run of a period a different thing from the same
 * threshold in the next: three daily votes on Monday are owed whatever happens on Tuesday, and a
 * Monday reward still waiting must not stand in the way of Tuesday's.
 *
 * Which period and which threshold are recorded rather than the commands, so the reward still goes
 * out after the period has ended and after the commands behind it have been reconfigured.
 */
data class PendingCumulativeReward(val period: LeaderboardType, val periodStart: Long, val votes: Int)

data class User(val uuid: UUID, var name: String, private val data: MutableList<Long>, var claimable: Int, private var pending: MutableList<PendingCumulativeReward>? = null)
{
	
	/**
	 * Records a vote and returns the timestamp it was recorded at.
	 *
	 * The caller needs that same reading to work out which cumulative periods the vote falls in.
	 * Reading the clock a second time, after midnight has passed, puts a vote cast at 23:59:59.999
	 * into the day that midnight started.
	 */
	fun voted(): Long
	{
		return voted(System.currentTimeMillis())
	}
	
	/**
	 * Records a vote at a given time. [voted] is the entry point everything else uses; this takes
	 * the time so a vote can be placed in a period without waiting for one to come round.
	 */
	internal fun voted(epoch: Long): Long
	{
		data += epoch
		
		return epoch
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
		pending = null
	}
	
	fun player() : OfflinePlayer
	{
		return Bukkit.getOfflinePlayer(uuid)
	}
	
	/**
	 * Records every threshold in [entries] this player's votes have just reached.
	 *
	 * Called once per vote, which is what makes an exact comparison enough. A vote appends one
	 * entry to [data], so the count since [since] moves up by exactly one and a threshold sits on
	 * that count for exactly one vote. Checking later, against the count of all the votes, missed
	 * every threshold that count had already moved past.
	 *
	 * Recording is kept apart from paying because most votes cannot be paid where they are cast.
	 * The player may be offline, or have no room for the rewards, and the count moves on either
	 * way. A threshold already waiting for this same run of the period is not recorded twice, so one
	 * crossing is one payout however many times the payout path runs before it succeeds. A run of
	 * the period that has not been paid yet counts as its own threshold.
	 */
	internal fun queueCrossedCumulativeRewards(period: LeaderboardType, since: Long, entries: List<CumulativeVoteCommands>)
	{
		val count = data.count { it >= since }
		
		for (entry in entries)
		{
			if (entry.votes == count && !isPending(period, since, entry.votes))
			{
				queue(PendingCumulativeReward(period, since, entry.votes))
			}
		}
	}
	
	/**
	 * The thresholds this player has reached and has not been handed over yet.
	 */
	internal fun pendingCumulativeRewards(): List<PendingCumulativeReward>
	{
		return pending ?: emptyList()
	}
	
	/**
	 * Hands every waiting reward whose period is in [periods] to [deliver], and drops it once
	 * [deliver] reports it was handed over.
	 *
	 * A [deliver] that returns false, or that throws, leaves its reward waiting for the next run, so
	 * a reward is only ever dropped when every command behind it was accepted. Dropping last is
	 * what makes the payout at-least-once rather than transactional: a command that did run before
	 * a later one failed runs again on the retry. Repeating a reward is the better failure of the
	 * two, but this cannot promise a reward arrives exactly once.
	 *
	 * @return How many rewards were handed over and dropped.
	 */
	internal fun drainCumulativeRewards(periods: Set<LeaderboardType>, deliver: (PendingCumulativeReward) -> Boolean): Int
	{
		var drained = 0
		
		// Over a copy, since handing a reward over drops it from the list being walked.
		for (reward in pendingCumulativeRewards().toList())
		{
			if (reward.period !in periods || !deliver(reward))
			{
				continue
			}
			
			pending?.remove(reward)
			drained++
		}
		
		return drained
	}
	
	private fun isPending(period: LeaderboardType, periodStart: Long, votes: Int): Boolean
	{
		return pending?.any { it.period == period && it.periodStart == periodStart && it.votes == votes } ?: false
	}
	
	private fun queue(reward: PendingCumulativeReward)
	{
		val rewards = pending ?: mutableListOf<PendingCumulativeReward>().also { pending = it }
		
		rewards.add(reward)
	}
	
}