package me.clip.voteparty.user

import me.clip.voteparty.VoteParty
import me.clip.voteparty.conf.sections.VoteSettings
import me.clip.voteparty.exte.formMessage
import me.clip.voteparty.leaderboard.LeaderboardType
import me.clip.voteparty.plugin.VotePartyPlugin
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.util.UUID

/*
0 = daily
1 = weekly
2 = monthly
3 = yearly
4 = total
 */
data class User(val uuid: UUID, var name: String, private val data: MutableList<Long>, private val cumulativeRewards: MutableList<DelayedReward>, var claimable: Int)
{
	
	fun voted()
	{
		data += System.currentTimeMillis()

		// I am so sorry for this...
		val player = player()

		if (!player.isOnline)
		{
			// Check for cumulative rewards
			val partyPlugin = Bukkit.getPluginManager().getPlugin("VoteParty") as VotePartyPlugin
			val vp = partyPlugin.voteParty ?: return

			val settings = vp.conf().getProperty(VoteSettings.CUMULATIVE_VOTE_REWARDS)

			if (settings.daily.enabled && settings.daily.entries.isNotEmpty())
			{
				settings.daily.entries.filter { entry -> entry.votes == vp.usersHandler.getVotesSince(player, LeaderboardType.DAILY.time.invoke()) }.forEach { entry ->
					cumulativeRewards.add(DelayedReward(0, entry.votes))
				}
			}

			if (settings.weekly.enabled && settings.weekly.entries.isNotEmpty())
			{
				settings.weekly.entries.filter { entry -> entry.votes == vp.usersHandler.getVotesSince(player, LeaderboardType.WEEKLY.time.invoke()) }.forEach { entry ->
					cumulativeRewards.add(DelayedReward(1, entry.votes))
				}
			}

			if (settings.monthly.enabled && settings.monthly.entries.isNotEmpty())
			{
				settings.monthly.entries.filter { entry -> entry.votes == vp.usersHandler.getVotesSince(player, LeaderboardType.MONTHLY.time.invoke()) }.forEach { entry ->
					cumulativeRewards.add(DelayedReward(2, entry.votes))
				}
			}

			if (settings.yearly.enabled && settings.yearly.entries.isNotEmpty())
			{
				settings.yearly.entries.filter { entry -> entry.votes == vp.usersHandler.getVotesSince(player, LeaderboardType.ANNUALLY.time.invoke()) }.forEach { entry ->
					cumulativeRewards.add(DelayedReward(3, entry.votes))
				}
			}

			if (settings.total.enabled && settings.total.entries.isNotEmpty())
			{
				settings.total.entries.filter { entry -> entry.votes == vp.usersHandler.getVotesSince(player, LeaderboardType.ALLTIME.time.invoke()) }.forEach { entry ->
					cumulativeRewards.add(DelayedReward(4, entry.votes))
				}
			}
		}
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
	}
	
	fun player() : OfflinePlayer
	{
		return Bukkit.getOfflinePlayer(uuid)
	}

	fun hasReward(): Boolean
	{
		return cumulativeRewards.isNotEmpty()
	}

	fun rewards(): List<DelayedReward>
	{
		return cumulativeRewards
	}

	fun removeCumulative(index: Int)
	{
		cumulativeRewards.removeAt(index)
	}

}
