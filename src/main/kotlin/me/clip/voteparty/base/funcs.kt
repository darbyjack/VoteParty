package me.clip.voteparty.base

import co.aikar.commands.ACFUtil
import co.aikar.commands.CommandIssuer
import co.aikar.locales.MessageKeyProvider
import me.clip.placeholderapi.PlaceholderAPI
import me.clip.voteparty.config.objects.Command
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
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

fun sendMessage(prefix: String, issuer: CommandIssuer, key: MessageKeyProvider, target: OfflinePlayer? = null, vararg replace: Any = emptyArray())
{
	var msg = issuer.manager.getLocales().getMessage(issuer, key)
	
	if (replace.isNotEmpty() && replace.size % 2 == 0)
	{
		msg = ACFUtil.replaceStrings(msg, *replace.map(Any::toString).toTypedArray())
	}
	
	issuer.sendMessage(formMessage(Bukkit.getOfflinePlayer(target?.uniqueId ?: issuer.uniqueId), prefix + msg))
}


fun Plugin.runTaskTimer(period: Long, task: BukkitRunnable.() -> Unit)
{
	object : BukkitRunnable()
	{
		override fun run()
		{
			task.invoke(this)
		}
	}.runTaskTimer(this, 0L, period)
}

fun Plugin.runTaskLater(delay: Long, task: () -> Unit)
{
	server.scheduler.runTaskLater(this, task, delay)
}


fun Collection<Command>.reduce(take: Int): Collection<Command>
{
	return filter { it.randomChance() }.shuffled().take(take)
}


fun item(type: Material, amount: Int, function: ItemMeta.() -> Unit = {}): ItemStack
{
	return ItemStack(type, amount).meta(function)
}

fun ItemStack.meta(function: ItemMeta.() -> Unit): ItemStack
{
	itemMeta = itemMeta?.apply(function)
	
	return this
}

var ItemMeta.name: String
	get() = displayName
	set(value)
	{
		setDisplayName(color(value))
	}