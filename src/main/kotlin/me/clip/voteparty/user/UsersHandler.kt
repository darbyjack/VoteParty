package me.clip.voteparty.user

import me.clip.voteparty.base.Addon
import me.clip.voteparty.base.State
import me.clip.voteparty.data.impl.DatabaseVotePlayerGson
import me.clip.voteparty.leaderboard.LeaderboardUser
import me.clip.voteparty.plugin.VotePartyPlugin
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

/**
 *
 * And I quote. "Wait till someone complains" - Glare 2020
 *
 */
class UsersHandler(override val plugin: VotePartyPlugin) : Addon, State, Listener
{

	private val database = DatabaseVotePlayerGson(plugin)
	private val cached = mutableMapOf<Any, User>()


	override fun load()
	{
		database.load()

		database.load(emptyList()).forEach()
		{ (_, data) ->
			data ?: return@forEach

			cached[data.uuid] = data
            cached[data.name.lowercase(Locale.getDefault())] = data
		}
		server.pluginManager.registerEvents(this, plugin)
	}

	override fun kill()
	{
		saveAll()
		database.kill()

		cached.clear()

		HandlerList.unregisterAll(this)
	}


	operator fun get(uuid: UUID): User
	{
		return cached.getOrPut(uuid)
		{
			User(uuid, "", mutableListOf(), 0)
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

	/**
	 * Get the amount of votes a player has made since the given time.
	 * @param offlinePlayer The player to check.
	 * @param amount The amount of time to check since.
	 * @param unit The unit of time to check since.
	 * @return The amount of votes the player has made since the given time.
	 */
	fun getVoteCountSince(offlinePlayer: OfflinePlayer, amount: Long, unit: TimeUnit): Int
	{
		val timeEpoch = Instant.now().minusMillis(TimeUnit.MILLISECONDS.convert(amount, unit)).toEpochMilli()

		return getVoteCountSince(offlinePlayer, timeEpoch)
	}

	/**
	 * Get the amount of votes a player has made since the given time.
	 * @param offlinePlayer The player to check.
	 * @param time The time to check since.
	 * @return The amount of votes the player has made since the given time.
	 */
	fun getVoteCountSince(offlinePlayer: OfflinePlayer, time: LocalDateTime): Int
	{
		val timeEpoch = time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

		return getVoteCountSince(offlinePlayer, timeEpoch)
	}

	/**
	 * Get the amount of votes a player has made since the given time.
	 * @param offlinePlayer The player to check.
	 * @param epoch The epoch time to check since.
	 * @return The amount of votes the player has made since the given time.
	 */
	fun getVoteCountSince(offlinePlayer: OfflinePlayer, epoch: Long) : Int
	{
		return get(offlinePlayer).votes().count { it >= epoch }
	}

	/**
	 * Get the amount of votes a player has made in a range of time.
	 * @param offlinePlayer The player to check.
	 * @param start The start of the range.
	 * @param end The end of the range.
	 * @return The amount of votes the player has made in the given range.
	 */
	fun getVoteCountWithinRange(offlinePlayer: OfflinePlayer, start: LocalDateTime, end: LocalDateTime): Int
	{
		val startEpoch = start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
		val endEpoch = end.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

		return  getVoteCountWithinRange(offlinePlayer, startEpoch, endEpoch)
	}

	/**
	 * Get the amount of votes a player has made in a range of time.
	 * @param offlinePlayer The player to check.
	 * @param startEpoch The start of the range.
	 * @param endEpoch The end of the range.
	 * @return The amount of votes the player has made in the given range.
	 */
	fun getVoteCountWithinRange(offlinePlayer: OfflinePlayer, startEpoch: Long, endEpoch: Long): Int
	{
		if (endEpoch < startEpoch) {
			return getVoteCountSince(offlinePlayer, startEpoch)
		}

		return get(offlinePlayer).votes().count { voteEpoch -> voteEpoch in startEpoch until endEpoch }
	}

	/**
	 * Get a list of all players who have voted since the given time.
	 * @param amount The amount of time to check since.
	 * @param unit The unit of time to check since.
	 */
	fun getPlayersWhoVotedSince(amount: Long, unit: TimeUnit) : List<UUID>
	{
		val timeEpoch = Instant.now().minusMillis(TimeUnit.MILLISECONDS.convert(amount, unit)).toEpochMilli()

		return getPlayersWhoVotedSince(timeEpoch)
	}

	/**
	 * Get a list of all players who have voted since the given time.
	 * @param epoch The epoch time to check since.
	 * @return A list of players who have voted since the given time.
	 */
	fun getPlayersWhoVotedSince(epoch: Long): List<UUID>
	{
		return cached.values.distinct().filter { getVoteCountSince(it.player(), epoch) > 0 }.map { it.uuid }
	}

	/**
	 * Get a list of all users who have voted within the given range with the amount of votes they have made in that range.
	 * @param start The start of the range.
	 * @param end The end of the range.
	 * @return A list of users who have voted within the given range.
	 */
	fun getUsersWithVotesWithinRange(start: LocalDateTime, end: LocalDateTime): List<LeaderboardUser>
	{
		val startEpoch = start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
		val endEpoch = end.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

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
					voteEpoch in startEpoch until endEpoch
				}

				LeaderboardUser(user, votes)
			}
			.sortedByDescending()
			{ user ->
				user.votes
			}
			.toList()
	}

	/**
	 * Get a list of all users who have voted since the given time with the amount of votes they have made since then.
	 * @param time The time to check since.
	 * @return A list of users who have voted since the given time.
	 */
	fun getUsersWithVotesSince(time: LocalDateTime): List<LeaderboardUser>
	{
		val timeEpoch = time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

		return getUsersWithVotesSince(timeEpoch)
	}

	/**
	 * Get a list of all users who have voted since the given time with the amount of votes they have made since then.
	 * @param epoch The epoch time to check since.
	 * @return A list of users who have voted since the given time.
	 */
	fun getUsersWithVotesSince(epoch: Long) : List<LeaderboardUser>
	{
		return cached.values.asSequence().distinct()
			.filter { it.votes().isNotEmpty() }
			.map { user -> LeaderboardUser(user, user.votes().count { voteTime -> voteTime >= epoch }) }
			.sortedByDescending { it.votes }
			.toList()
	}

	/**
	 * Get the total amount of votes made by all users ever.
	 * @return The total amount of votes made by all users ever.
	 */
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
			val new = User(player.uniqueId, player.name, mutableListOf(), 0)

			cached[new.uuid] = new
            cached[new.name.lowercase(Locale.getDefault())] = new
		}
	}

}
