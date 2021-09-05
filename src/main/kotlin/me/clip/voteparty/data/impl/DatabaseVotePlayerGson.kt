package me.clip.voteparty.data.impl

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import me.clip.voteparty.data.base.DatabaseVotePlayer
import me.clip.voteparty.plugin.VotePartyPlugin
import me.clip.voteparty.user.User
import java.util.UUID
import java.util.logging.Level

internal class DatabaseVotePlayerGson(override val plugin: VotePartyPlugin) : DatabaseVotePlayer
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


	override fun load(uuid: UUID): User?
	{
		return try
		{
			gson.fromJson(plugin.dataFolder.resolve("players").resolve("$uuid.json").readText(), User::class.java)
		}
		catch (ex: Exception)
		{
			logger.log(Level.SEVERE, "failed to load player:$uuid", ex)
			null
		}
	}

	override fun save(data: User)
	{
		try
		{
			plugin.dataFolder.resolve("players").resolve("${data.uuid}.json").writeText(gson.toJson(data, User::class.java))
		}
		catch (ex: Exception)
		{
			logger.log(Level.SEVERE, "failed to save player:${data.uuid}", ex)
		}
	}

	override fun reset(data: User)
	{
		data.reset()
	}

	override fun load(uuid: Collection<UUID>): Map<UUID, User?>
	{
		if (uuid.isNotEmpty())
		{
			return super.load(uuid)
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

}
