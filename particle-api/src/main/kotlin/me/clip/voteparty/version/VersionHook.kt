package me.clip.voteparty.version

import org.bukkit.Color
import org.bukkit.Location

interface VersionHook
{
	
	/**
	 * Displays a particle named by [type], which is a raw configuration value.
	 *
	 * Implementations own the whole job of turning that name into something the running server
	 * understands, and must not throw when they cannot: a particle name this Minecraft version
	 * does not have is a configuration detail, not a reason to abort whatever the caller was
	 * doing. An unrecognised name is skipped.
	 */
	fun display(type: String, location: Location, offsetX: Double, offsetY: Double, offsetZ: Double, speed: Double, count: Int, color: Color? = null)
	
}