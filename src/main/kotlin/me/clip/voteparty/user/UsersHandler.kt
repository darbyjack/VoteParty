package me.clip.voteparty.user

import me.clip.voteparty.base.Addon
import me.clip.voteparty.base.State
import me.clip.voteparty.conf.sections.VoteSettings
import me.clip.voteparty.data.impl.DatabaseRecentVotersGson
import me.clip.voteparty.data.impl.DatabaseVotePlayerGson
import me.clip.voteparty.exte.formMessage
import me.clip.voteparty.leaderboard.LeaderboardUser
import me.clip.voteparty.plugin.VotePartyPlugin
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.logging.Level

/**
 *
 * And I quote. "Wait till someone complains" - Glare 2020
 *
 */
class UsersHandler(override val plugin: VotePartyPlugin) : Addon, State, Listener
{

	private val database = DatabaseVotePlayerGson(plugin)
	private val recentVotersDatabase = DatabaseRecentVotersGson(plugin)
	private val cached = mutableMapOf<Any, User>()
	private var recentVoters : RecentVoters? = null

	override fun load()
	{
		database.load()

		database.load(emptyList()).forEach()
		{ (_, data) ->
			data ?: return@forEach

			cached[data.uuid] = data
            cached[data.name.lowercase(Locale.getDefault())] = data
		}

		recentVotersDatabase.load()
		recentVoters = recentVotersDatabase.loadVoters()

		val recentCount = party.conf().getProperty(VoteSettings.RECENT_VOTE_COUNT)

		// If there was no file, create an empty object
		if (recentVoters == null) {
			recentVoters = RecentVoters(recentCount, mutableMapOf())
		} else if (recentCount != recentVoters!!.size()) { // We know recentVoters isn't null, so we can cast
			// Update the size if the config has updated
			val newVoters = RecentVoters(recentCount, recentVoters!!.recentVoters)
			recentVoters = newVoters
		}


		server.pluginManager.registerEvents(this, plugin)
	}

	override fun kill()
	{
		saveAll()
		database.kill()

		recentVoters?.let { recentVotersDatabase.save(it) }
		recentVotersDatabase.kill()

		cached.clear()

		HandlerList.unregisterAll(this)
	}


	operator fun get(uuid: UUID): User
	{
		return cached.getOrPut(uuid)
		{
			User(uuid, "", mutableListOf(), mutableListOf(), 0)
		}
	}

	operator fun get(name: String): User?
	{
        return cached[name.lowercase(Locale.getDefault())]
	}

	operator fun get(player: OfflinePlayer): User
	{
		return get(player.uniqueId)
	}


	fun reset(player: OfflinePlayer)
	{
		get(player).reset()
	}

	fun saveAll()
	{
		database.save(cached.values.distinct())
	}

	fun save(user: User)
	{
		database.save(user)
	}

	fun getVotesWithinRange(offlinePlayer: OfflinePlayer, amount: Long, unit: TimeUnit): Int
	{
		val time = Instant.now().minusMillis(TimeUnit.MILLISECONDS.convert(amount, unit)).toEpochMilli()

		return get(offlinePlayer).votes().count { it > time }
	}

	fun getPlayersVotedWithinRange(amount: Long, unit: TimeUnit) : List<UUID>
	{
		return cached.values.distinct().filter { getVotesWithinRange(it.player(), amount, unit) > 0 }.map { it.uuid }
	}

	fun getVotesWithinRange(offlinePlayer: OfflinePlayer, epoch: Long) : Int
	{
		return get(offlinePlayer).votes().count { it > epoch }
	}

	fun getVotesWithinRange(epoch: Long) : List<LeaderboardUser>
	{
		return cached.values.asSequence().distinct()
				.filter { it.votes().isNotEmpty() }
				.map { LeaderboardUser(it, it.votes().count { it >= epoch }) }
				.sortedByDescending { it.votes }
				.toList()
	}

	fun getVotesSince(time: LocalDateTime): List<LeaderboardUser>
	{
		val timeEpoch = time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

		return cached.values.asSequence()
				.distinct()
				.filter()
				{ user ->
					user.votes().isNotEmpty()
				}
				.map()
				{ user ->

					val votes = user.votes().count()
					{ voteEpoch ->
						voteEpoch >= timeEpoch
					}

					LeaderboardUser(user, votes)
				}
				.sortedByDescending()
				{ user ->
					user.votes
				}
				.toList()
	}

	fun getVotesSince(user: OfflinePlayer, time: LocalDateTime): Int
	{
		val timeEpoch = time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

		return get(user).votes().count { voteEpoch -> voteEpoch > timeEpoch }
	}


	fun getRecentVoters(): RecentVoters? {
		return recentVoters
	}

	fun getTotalVotes(): Int {
		return cached.values.distinct().sumOf { it.votes().size }
	}


	@EventHandler
	fun PlayerJoinEvent.onJoin()
	{
		val old = cached[player.uniqueId]

		if (old != null && old.name != player.name) {
            cached -= old.name.lowercase(Locale.getDefault())
            cached[player.name.lowercase(Locale.getDefault())] = old
            old.name = player.name
        }

		if (old == null)
		{
			val new = User(player.uniqueId, player.name, mutableListOf(), mutableListOf(), 0)

			cached[new.uuid] = new
            cached[new.name.lowercase(Locale.getDefault())] = new
		} else {
			// Handle cumulative rewards
			if (old.hasReward()) {
				val settings = party.conf().getProperty(VoteSettings.CUMULATIVE_VOTE_REWARDS)

				val rewards = old.rewards()

				// Check if each type is enabled and has rules
				// Filter out all pending rewards by type
				// For each filtered reward check to see if there is a rule of the specified type with the same number of votes
				// If there is, trigger the commands for that rule
				if (settings.daily.enabled && settings.daily.entries.isNotEmpty()) {
					rewards.filter { reward -> reward.type == 0 }.forEach { reward ->
						settings.daily.entries.filter { entry -> entry.votes == reward.votes }.forEach { entry ->
							old.removeCumulative(rewards.indexOf(reward)) // I have no idea if this is going to work

							entry.commands.forEach()
							{ command ->
								server.dispatchCommand(server.consoleSender, formMessage(player, command))
							}
						}
					}
				}

				if (settings.weekly.enabled && settings.weekly.entries.isNotEmpty()) {
					rewards.filter { reward -> reward.type == 1 }.forEach { reward ->
						settings.weekly.entries.filter { entry -> entry.votes == reward.votes }.forEach { entry ->
							old.removeCumulative(rewards.indexOf(reward)) // I have no idea if this is going to work

							entry.commands.forEach()
							{ command ->
								server.dispatchCommand(server.consoleSender, formMessage(player, command))
							}
						}
					}
				}

				if (settings.monthly.enabled && settings.monthly.entries.isNotEmpty()) {
					rewards.filter { reward -> reward.type == 2 }.forEach { reward ->
						settings.monthly.entries.filter { entry -> entry.votes == reward.votes }.forEach { entry ->
							old.removeCumulative(rewards.indexOf(reward)) // I have no idea if this is going to work

							entry.commands.forEach()
							{ command ->
								server.dispatchCommand(server.consoleSender, formMessage(player, command))
							}
						}
					}
				}

				if (settings.yearly.enabled && settings.yearly.entries.isNotEmpty()) {
					rewards.filter { reward -> reward.type == 3 }.forEach { reward ->
						settings.yearly.entries.filter { entry -> entry.votes == reward.votes }.forEach { entry ->
							old.removeCumulative(rewards.indexOf(reward)) // I have no idea if this is going to work

							entry.commands.forEach()
							{ command ->
								server.dispatchCommand(server.consoleSender, formMessage(player, command))
							}
						}
					}
				}

				if (settings.total.enabled && settings.total.entries.isNotEmpty()) {
					rewards.filter { reward -> reward.type == 4 }.forEach { reward ->
						settings.total.entries.filter { entry -> entry.votes == reward.votes }.forEach { entry ->
							old.removeCumulative(rewards.indexOf(reward)) // I have no idea if this is going to work

							entry.commands.forEach()
							{ command ->
								server.dispatchCommand(server.consoleSender, formMessage(player, command))
							}
						}
					}
				}
			}
		}
	}

}
