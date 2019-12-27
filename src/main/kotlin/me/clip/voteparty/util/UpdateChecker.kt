package me.clip.voteparty.util

import com.google.gson.reflect.TypeToken
import kong.unirest.Unirest
import me.clip.voteparty.VoteParty
import org.bukkit.plugin.Plugin
import java.lang.Exception

object UpdateChecker
{
	
	private const val URL = "https://api.spiget.org/v2/resources/%d/versions?size=1&sort=-releaseDate"
	
	
	fun check(plugin: Plugin, id: Int, complete: (result: UpdateResult) -> Unit)
	{
		check(plugin.description.version, id, complete)
	}
	
	@Suppress("FoldInitializerAndIfToElvis")
	fun check(version: String, id: Int, complete: (result: UpdateResult) -> Unit)
	{
		Unirest.get(URL.format(id)).header("User-Agent", "CHOCO-update-checker").asStringAsync().whenCompleteAsync()
		rest@{ response, exception ->
			
			val body = response.body
			if (body == null || exception != null)
			{
				return@rest complete.invoke(UpdateResult(UpdateReason.EXCEPTIONS, exception.message))
			}
			
			val old = version
			val new = try
			{
				checkNotNull(VoteParty.GSON.fromJson<List<Version>>(body, object : TypeToken<List<Version>>() {}.type)?.firstOrNull()?.name)
			}
			catch (ex: Exception)
			{
				return@rest complete.invoke(UpdateResult(UpdateReason.EXCEPTIONS, ex.message))
			}
			
			if (old == new)
			{
				return@rest complete.invoke(UpdateResult(UpdateReason.UP_TO_DATE, new))
			}
			
			val oldVersion = old.split('.').mapNotNull(String::toIntOrNull)
			val newVersion = new.split('.').mapNotNull(String::toIntOrNull)
			
			if (newVersion.size > oldVersion.size)
			{
				return@rest complete.invoke(UpdateResult(UpdateReason.NEW_UPDATE, new))
			}
			
			oldVersion.forEachIndexed()
			{ index, value ->
				if (value < newVersion[index])
				{
					return@rest complete.invoke(UpdateResult(UpdateReason.NEW_UPDATE, new))
				}
			}
			
			complete.invoke(UpdateResult(UpdateReason.UNRELEASED, new))
		}
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