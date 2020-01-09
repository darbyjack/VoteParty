package me.clip.voteparty.database.voteplayer

import me.clip.voteparty.voteplayer.VotePlayer
import java.io.IOException

interface VotePlayerProvider
{
	@Throws(IOException::class)
	fun createContainer()
	
	@Throws(IOException::class)
	fun playerExists(id: String): Boolean
	
	@Throws(IOException::class)
	fun getAllPlayers(): MutableList<VotePlayer>?
	
	@Throws(IOException::class)
	fun getPlayer(id: String): VotePlayer
	
	@Throws(IOException::class)
	fun createPlayer(id: String, data: String)
	
	@Throws(IOException::class)
	fun updatePlayer(id: String, data: String)
}