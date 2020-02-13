package me.clip.voteparty.util

import org.bukkit.plugin.Plugin
import java.net.URL

object UpdateChecker
{
	
	private const val API = "https://api.spigotmc.org/legacy/update.php?resource=987"
	
	
	fun check(plugin: Plugin, id: Int, complete: (result: UpdateResult) -> Unit)
	{
		plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable
		{
			complete.invoke(check(plugin.description.version, id))
		})
	}
	
	/**
	 * runs the update check on the current thread, and returns an appropriate result
	 */
	fun check(version: String, id: Int): UpdateResult
	{
		val response = try
		{
			URL(API.format(id)).readText()
		}
		catch (ex: Exception)
		{
			return UpdateResult.EXCEPTIONS(throwable = ex)
		}
		
		
		val new = try
		{
			response
		}
		catch (ex: Exception)
		{
			return UpdateResult.EXCEPTIONS(throwable = ex)
		}
		
		if (version == new)
		{
			return UpdateResult.UP_TO_DATE
		}
		
		val oldVersion = version.split('.').mapNotNull(String::toIntOrNull)
		val newVersion = new.split('.').mapNotNull(String::toIntOrNull)
		
		if (newVersion.size > oldVersion.size)
		{
			return UpdateResult.NEW_UPDATE(version = new)
		}
		
		return UpdateResult.UNRELEASED
	}
	
	
	sealed class UpdateResult
	{
		
		abstract val message: String
		
		
		object UP_TO_DATE : UpdateResult()
		{
			override val message = "Version is up to date"
			
			
			override fun toString(): String
			{
				return "UP_TO_DATE(${UNRELEASED.message})"
			}
			
		}
		
		object UNRELEASED : UpdateResult()
		{
			override val message = "You're on a newer version than latest"
			
			override fun toString(): String
			{
				return "UNRELEASED($message)"
			}
			
		}
		
		data class NEW_UPDATE(override val message: String = "There is an update available", val version: String)
			: UpdateResult()
		
		data class EXCEPTIONS(override val message: String = "Failed to check for an update", val throwable: Throwable)
			: UpdateResult()
		
	}
	
}