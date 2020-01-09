package me.clip.voteparty.voteplayer

import me.clip.voteparty.VoteParty
import org.bukkit.OfflinePlayer
import java.io.IOException
import java.util.UUID

class VotePlayerHandler(val voteParty: VoteParty)
{
	private var votePlayers: MutableList<VotePlayer>? = null
	
	@Throws(IOException::class)
	fun saveData()
	{
		voteParty.DatabaseAdapter.votePlayerAdapter?.savePlayers(votePlayers)
	}
	
	fun addPlayer(votePlayer: VotePlayer)
	{
		votePlayers?.add(votePlayer)
	}
	
	fun getPlayer(offlinePlayer: OfflinePlayer): VotePlayer?
	{
		return votePlayers?.stream()?.filter()
		{
			it.id == offlinePlayer.uniqueId
		}?.findFirst()?.orElse(null)
	}
	
	fun getPlayer(uuid: UUID): VotePlayer?
	{
		return votePlayers?.stream()?.filter()
		{
			it.id == uuid
		}?.findFirst()?.orElse(null)
	}
	
	fun getVotes(offlinePlayer: OfflinePlayer) : Int
	{
		return getPlayer(offlinePlayer)?.votes ?: 0
	}
	
	init
	{
		try
		{
			votePlayers = voteParty.DatabaseAdapter.votePlayerAdapter?.allPlayers
		} catch (ex: IOException)
		{
			ex.printStackTrace()
		}
	}
}
