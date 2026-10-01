package me.clip.voteparty.user

import me.clip.voteparty.conf.objects.CumulativeVoteCommands
import me.clip.voteparty.leaderboard.LeaderboardType
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.util.UUID

/**
 * A cumulative reward threshold a player has reached and has not been handed over yet.
 *
 * Which period it was reached in and which threshold it was are both recorded rather than the
 * commands, so the reward still goes out after the period has ended and after the commands behind
 * it have been reconfigured.
 */
data class PendingCumulativeReward(val period: LeaderboardType, val votes: Int)

data class User(val uuid: UUID, var name: String, private val data: MutableList<Long>, var claimable: Int, private var pending: MutableList<PendingCumulativeReward>? = null)
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
	 * way. A threshold already waiting is not recorded twice, so one crossing is one payout however
	 * many times the payout path runs before it succeeds.
	 */
	internal fun queueCrossedCumulativeRewards(period: LeaderboardType, since: Long, entries: List<CumulativeVoteCommands>)
	{
		val count = data.count { it >= since }
		
		for (entry in entries)
		{
			if (entry.votes == count && !isPending(period, entry.votes))
			{
				queue(PendingCumulativeReward(period, entry.votes))
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
	 * [deliver] has returned.
	 *
	 * Dropping last is deliberate, and it makes this at-least-once rather than transactional. A
	 * [deliver] that throws leaves its reward waiting for the next run, so any commands that
	 * already went out before the throw go out again. Repeating a reward is a better failure than
	 * losing one, but this cannot promise a reward arrives exactly once.
	 *
	 * @return How many rewards were handed over.
	 */
	internal fun drainCumulativeRewards(periods: Set<LeaderboardType>, deliver: (PendingCumulativeReward) -> Unit): Int
	{
		var drained = 0
		
		// Over a copy, since handing a reward over drops it from the list being walked.
		for (reward in pendingCumulativeRewards().toList())
		{
			if (reward.period !in periods)
			{
				continue
			}
			
			deliver(reward)
			
			pending?.remove(reward)
			drained++
		}
		
		return drained
	}
	
	private fun isPending(period: LeaderboardType, votes: Int): Boolean
	{
		return pending?.any { it.period == period && it.votes == votes } ?: false
	}
	
	private fun queue(reward: PendingCumulativeReward)
	{
		val rewards = pending ?: mutableListOf<PendingCumulativeReward>().also { pending = it }
		
		rewards.add(reward)
	}
	
}