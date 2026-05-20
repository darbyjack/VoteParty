package me.clip.voteparty.version

import org.bukkit.Bukkit

internal data class MinecraftVersion(
	val major: Int,
	val minor: Int,
	val patch: Int,
) : Comparable<MinecraftVersion>
{

	override fun compareTo(other: MinecraftVersion): Int
	{
		return compareValuesBy(this, other, MinecraftVersion::major, MinecraftVersion::minor, MinecraftVersion::patch)
	}

	fun isAtLeast(other: MinecraftVersion): Boolean
	{
		return this >= other
	}

	companion object
	{

		val V1_13 = MinecraftVersion(1, 13, 0)

		fun current(): MinecraftVersion
		{
			return parse(Bukkit.getServer().minecraftVersionCompat())
		}

		fun parse(rawVersion: String): MinecraftVersion
		{
			val cleanVersion = rawVersion
				.substringBefore('-')
				.substringBefore(' ')
				.trim()

			val parts = cleanVersion
				.split('.')
				.map { part -> part.takeWhile { it.isDigit() } }
				.mapNotNull { it.toIntOrNull() }

			return MinecraftVersion(
				major = parts.getOrNull(0) ?: 1,
				minor = parts.getOrNull(1) ?: 0,
				patch = parts.getOrNull(2) ?: 0,
			)
		}

	}

}

/**
 * Bukkit/Spigot/Paper expose Server#getMinecraftVersion() on modern APIs.
 *
 * VoteParty compiles common code against the 1.8.8 API, where that method is not available
 * at compile time, so call it reflectively and fall back to Bukkit#getBukkitVersion().
 */
private fun org.bukkit.Server.minecraftVersionCompat(): String
{
	return try
	{
		val method = javaClass.methods.firstOrNull { method ->
			method.name == "getMinecraftVersion" &&
				method.parameterCount == 0 &&
				method.returnType == String::class.java
		}

		method?.invoke(this) as? String ?: Bukkit.getBukkitVersion()
	}
	catch (_: ReflectiveOperationException)
	{
		Bukkit.getBukkitVersion()
	}
	catch (_: SecurityException)
	{
		Bukkit.getBukkitVersion()
	}
}