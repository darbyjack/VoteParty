package me.clip.voteparty.database.impl

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import me.clip.voteparty.database.base.DatabaseProvider
import me.clip.voteparty.plugin.VotePartyPlugin
import me.clip.voteparty.voteplayer.VotePlayer
import java.util.UUID
import java.util.logging.Level

class DatabaseVotePlayerGson(override val plugin: VotePartyPlugin) : DatabaseProvider<Gson>
{
	
	private lateinit var gson: Gson
	
	
	override fun load()
	{
		val builder = GsonBuilder().disableHtmlEscaping().enableComplexMapKeySerialization().setPrettyPrinting().serializeNulls()
		gson = builder.create()
		
		plugin.dataFolder.resolve("players").mkdirs()
	}
	
	override fun kill()
	{
	
	}
	
	
	override fun load(uuid: UUID): VotePlayer?
	{
		return try
		{
			gson.fromJson(plugin.dataFolder.resolve("players").resolve("$uuid.json").readText(), VotePlayer::class.java)
		}
		catch (ex: Exception)
		{
			logger.log(Level.SEVERE, "failed to load player:$uuid", ex)
			null
		}
	}
	
	override fun save(data: VotePlayer)
	{
		try
		{
			plugin.dataFolder.resolve("players").resolve("${data.uuid}.json").writeText(gson.toJson(data, VotePlayer::class.java))
		}
		catch (ex: Exception)
		{
			logger.log(Level.SEVERE, "failed to save player:${data.uuid}", ex)
		}
	}
	
	override fun loadBulk(uuid: Collection<UUID>): Map<UUID, VotePlayer?>
	{
		if (uuid.isNotEmpty())
		{
			return super.loadBulk(uuid)
		}
		
		
		val files = plugin.dataFolder.resolve("players").listFiles() ?: return emptyMap()
		
		return files.mapNotNull()
		{
			try
			{
				UUID.fromString(it.nameWithoutExtension)
			}
			catch (ex: Exception)
			{
				null
			}
		}.associateWith(::load)
	}
	
	
	override fun get(): Gson?
	{
		return gson
	}
	
}