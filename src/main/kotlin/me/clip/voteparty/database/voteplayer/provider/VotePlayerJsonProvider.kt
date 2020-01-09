package me.clip.voteparty.database.voteplayer.provider

import com.google.gson.Gson
import me.clip.voteparty.VoteParty.Companion.GSON
import me.clip.voteparty.database.voteplayer.VotePlayerProvider
import me.clip.voteparty.voteplayer.VotePlayer
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.ArrayList
import java.util.Arrays
import java.util.Objects

class VotePlayerJsonProvider(private val dataFolder: File) : VotePlayerProvider
{
	private val gson: Gson = GSON
	
	@Throws(IOException::class)
	override fun createContainer()
	{
		if (!dataFolder.exists())
		{
			dataFolder.mkdir()
		}
	}
	
	override fun playerExists(id: String): Boolean
	{
		return dataFolder.resolve("$id.json").exists()
	}
	
	@Throws(IOException::class)
	override fun getAllPlayers(): MutableList<VotePlayer>
	{
		val players: MutableList<VotePlayer> = ArrayList()
		
		 dataFolder.resolve("data").listFiles()?.forEach()
		{
			players.add(gson.fromJson(InputStreamReader(FileInputStream(it), StandardCharsets.UTF_8), VotePlayer::class.java))
		}
		return players
	}
	
	@Throws(IOException::class)
	override fun getPlayer(id: String): VotePlayer
	{
		
		if (playerExists(id))
		{
			return gson.fromJson(InputStreamReader(FileInputStream(dataFolder.resolve("$id.json").absoluteFile), StandardCharsets.UTF_8), VotePlayer::class.java)
		}
	}
	
	@Throws(IOException::class)
	override fun createPlayer(id: String, data: String)
	{
		if (playerExists(id))
		{
			return
		} else
		{
			writePlayerFile(File(dataFolder, "$id.json"), data)
		}
	}
	
	@Throws(IOException::class)
	override fun updatePlayer(id: String, data: String)
	{
		val file = File(dataFolder, "$id.json")
		deletePlayer(file)
		writePlayerFile(file, data)
	}
	
	@Throws(IOException::class)
	private fun writePlayerFile(file: File, data: String)
	{
		Files.write(Paths.get(file.path), data.toByteArray(StandardCharsets.UTF_8))
	}
	
	private fun deletePlayer(file: File)
	{
		if (file.exists()) file.delete()
	}
	
}