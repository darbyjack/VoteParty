package me.clip.voteparty.update

import com.google.gson.reflect.TypeToken
import kong.unirest.Unirest
import me.clip.voteparty.VoteParty
import org.bukkit.plugin.Plugin

object UpdateChecker
{
	
	private const val URL = "https://api.spiget.org/v2/resources/%d/versions?size=1&sort=-releaseDate"
	private val LIST_TYPE = object : TypeToken<List<Version>>() {}.type
	
	
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
			return complete.invoke(UpdateResult.EXCEPTIONS(throwable = ex))
		}
		
		
		val old = version
		val new = try
		{
			checkNotNull(VoteParty.GSON.fromJson<List<Version>>(response, LIST_TYPE)?.firstOrNull()?.name)
		} catch (ex: Exception)
		{
			return complete.invoke(UpdateResult.EXCEPTIONS(throwable = ex))
		}
		
		if (old == new)
		{
			return complete.invoke(UpdateResult.UP_TO_DATE)
		}
		
		val oldVersion = old.split('.').mapNotNull(String::toIntOrNull)
		val newVersion = new.split('.').mapNotNull(String::toIntOrNull)
		
		if (newVersion.size > oldVersion.size)
		{
			return complete.invoke(UpdateResult.NEW_UPDATE(version = new))
		}
		
		oldVersion.forEachIndexed()
		{ index, value ->
			if (value >= newVersion[index])
			{
				return@forEachIndexed
			}
			
			return complete.invoke(UpdateResult.NEW_UPDATE(version = new))
		}
		
		return complete.invoke(UpdateResult.UNRELEASED)
	}
	
	
	private data class Version(val name: String?)
	
	sealed class UpdateResult
	{
		
		abstract val message: String
		
		
		object UP_TO_DATE : UpdateResult()
		{
			override val message = "Version is up to date"
		}
		
		object UNRELEASED : UpdateResult()
		{
			override val message = "You're on a newer version than latest"
		}
		
		class NEW_UPDATE(override val message: String = "There is an update available", val version: String)
			: UpdateResult()
		
		class EXCEPTIONS(override val message: String = "Failed to check for an update", val throwable: Throwable)
			: UpdateResult()
		
	}
	
}