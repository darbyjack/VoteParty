package me.clip.voteparty.user

import me.clip.voteparty.base.Addon
import me.clip.voteparty.base.State
import me.clip.voteparty.conf.sections.StorageSettings
import me.clip.voteparty.data.base.DatabaseVotePlayer
import me.clip.voteparty.data.impl.DatabaseVotePlayerGson
import me.clip.voteparty.data.impl.DatabaseVotePlayerMySQL
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

	private lateinit var database : DatabaseVotePlayer
	private val cached = mutableMapOf<Any, User>()


	override fun load()
	{
		database = if (party.conf().getProperty(StorageSettings.BACKEND).equals("mysql", true)) {
			DatabaseVotePlayerMySQL(plugin)
		} else {
			DatabaseVotePlayerGson(plugin)
		}

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
