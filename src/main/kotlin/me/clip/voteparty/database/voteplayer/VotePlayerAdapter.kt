package me.clip.voteparty.database.voteplayer

import me.clip.voteparty.VoteParty
import me.clip.voteparty.VoteParty.Companion.GSON
import me.clip.voteparty.database.voteplayer.provider.VotePlayerJsonProvider
import me.clip.voteparty.voteplayer.VotePlayer
import java.io.File
import java.io.IOException

class VotePlayerAdapter(voteParty: VoteParty)
{
	val provider: VotePlayerProvider
	
	@Throws(IOException::class)
	fun createContainer()
	{
		provider.createContainer()
	}
	
	@Throws(IOException::class)
	fun playerExists(id: String): Boolean
	{
		return provider.playerExists(id)
	}
	
	@get:Throws(IOException::class)
	val allPlayers: MutableList<VotePlayer>?
		get()
		= provider.getAllPlayers()
	
	@Throws(IOException::class)
	fun getPlayer(id: String): VotePlayer
	{
		return provider.getPlayer(id)
	}
	
	@Throws(IOException::class)
	fun savePlayers(players: MutableList<VotePlayer>?)
	{
		if (players != null)
		{
			for (player in players)
			{
				savePlayer(player)
			}
		}
	}
	
	@Throws(IOException::class)
	fun savePlayer(player: VotePlayer)
	{
		if (!playerExists(player.id.toString()))
		{
			createPlayer(player)
		} else
		{
			updatePlayer(player)
		}
	}
	
	@Throws(IOException::class)
	fun createPlayer(player: VotePlayer)
	{
		provider.createPlayer(player.id.toString(), GSON.toJson(player, VotePlayer::class.java))
	}
	
	@Throws(IOException::class)
	fun updatePlayer(player: VotePlayer)
	{
		provider.updatePlayer(player.id.toString(), GSON.toJson(player, VotePlayer::class.java))
	}
	
	init
	{
		val dataFolder = File(voteParty.plugin.dataFolder, "data")
		provider = VotePlayerJsonProvider(dataFolder)
	}
}