package me.clip.voteparty.update

import com.google.gson.reflect.TypeToken
import kong.unirest.Unirest
import me.clip.voteparty.VoteParty
import org.bukkit.plugin.Plugin

object UpdateChecker
{
	
	private const val URL = "https://api.spiget.org/v2/resources/%d/versions?size=1&sort=-releaseDate"
	
	
	fun check(plugin: Plugin, id: Int, complete: (result: UpdateResult) -> Unit)
	{
		plugin.server.scheduler.runTaskAsynchronously(plugin)
		{ _ ->
			check(plugin.description.version, id, complete)
		}
	}
	
	@Suppress("FoldInitializerAndIfToElvis")
	fun check(version: String, id: Int, complete: (result: UpdateResult) -> Unit)
	{
		val response = try
		{
			Unirest.get(URL.format(id)).header("User-Agent", "CHOCO-update-checker").asString()?.body
		} catch (ex: Exception)
		{
			return complete.invoke(UpdateResult(UpdateReason.EXCEPTIONS, ex.message))
		}
		
		
		val old = version
		val new = try
		{
			checkNotNull(VoteParty.GSON.fromJson<List<Version>>(response, object : TypeToken<List<Version>>()
			{}.type)?.firstOrNull()?.name)
		} catch (ex: Exception)
		{
			return complete.invoke(UpdateResult(UpdateReason.EXCEPTIONS, ex.message))
		}
		
		if (old == new)
		{
			return complete.invoke(UpdateResult(UpdateReason.UP_TO_DATE, new))
		}
		
		val oldVersion = old.split('.').mapNotNull(String::toIntOrNull)
		val newVersion = new.split('.').mapNotNull(String::toIntOrNull)
		
		if (newVersion.size > oldVersion.size)
		{
			return complete.invoke(UpdateResult(UpdateReason.NEW_UPDATE, new))
		}
		
		oldVersion.forEachIndexed()
		{ index, value ->
			if (value >= newVersion[index])
			{
				return@forEachIndexed
			}
			
			return complete.invoke(UpdateResult(UpdateReason.NEW_UPDATE, new))
		}
		
		return complete.invoke(UpdateResult(UpdateReason.UNRELEASED, new))
	}
	
	
	data class Version(val name: String?)
	
	data class UpdateResult(val reason: UpdateReason, val version: String? = null)
	
	enum class UpdateReason(val reason: String)
	{
		
		UP_TO_DATE("Version is up to date"),
		
		UNRELEASED("You're on a newer version than latest"),
		
		NEW_UPDATE("There is an update available"),
		
		EXCEPTIONS("Failed to check for an update"),
		
	}
	
}