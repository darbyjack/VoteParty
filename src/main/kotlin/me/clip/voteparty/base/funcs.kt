package me.clip.voteparty.base

import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandIssuer
import co.aikar.locales.MessageKeyProvider
import me.clip.placeholderapi.PlaceholderAPI
import me.clip.voteparty.conf.ConfigVoteParty
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable


fun color(message: String): String
{
	return ChatColor.translateAlternateColorCodes('&', message)
}

fun formMessage(player: OfflinePlayer, message: String): String
{
	return color(PlaceholderAPI.setPlaceholders(player, message))
}

fun BaseCommand.sendMessage(prefix: String, issuer: CommandIssuer, key: MessageKeyProvider, target: OfflinePlayer? = null)
{
	issuer.sendMessage(formMessage(Bukkit.getOfflinePlayer(target?.uniqueId ?: issuer.uniqueId), prefix + currentCommandManager.getLocales().getMessage(issuer, key.messageKey)))
}



fun Plugin.runTaskTimer(period: Int, task: BukkitRunnable.() -> Unit)
{
	object : BukkitRunnable()
	{
		override fun run()
		{
			task.invoke(this)
		}
	}.runTaskTimer(this, 0L, period.toLong())
}


fun Collection<ConfigVoteParty.Command>.reduce(take: Int): Collection<ConfigVoteParty.Command>
{
	return filter { it.randomChance() }.shuffled().take(take)
}