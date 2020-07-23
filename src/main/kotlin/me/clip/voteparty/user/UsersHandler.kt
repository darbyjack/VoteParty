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
import java.util.UUID
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
			cached[data.name.toLowerCase()] = data
		}
		server.pluginManager.registerEvents(this, plugin)
	}
	
	override fun kill()
	{
		database.save(cached.values.distinct())
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
		return cached[name.toLowerCase()]
	}
	
	operator fun get(player: OfflinePlayer): User
	{
		return get(player.uniqueId)
	}
	
	
	fun reset(player: OfflinePlayer)
	{
		get(player).reset()
	}
	
	fun getVotesWithinRange(offlinePlayer: OfflinePlayer, amount: Long, unit: TimeUnit): Int
	{
		val time = Instant.now().minusMillis(TimeUnit.MILLISECONDS.convert(amount, unit)).toEpochMilli()
		
		return get(offlinePlayer).votes().count { it > time }
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
	
	@EventHandler
	fun PlayerJoinEvent.onJoin()
	{
		val old = cached[player.uniqueId]
		
		if (old != null && old.name != player.name)
		{
			cached -= old.name.toLowerCase()
			cached[player.name.toLowerCase()] = old
		}
		
		if (old == null)
		{
			val new = User(player.uniqueId, player.name, mutableListOf(), 0)
			
			cached[new.uuid] = new
			cached[new.name.toLowerCase()] = new
		}
	}
	
}
