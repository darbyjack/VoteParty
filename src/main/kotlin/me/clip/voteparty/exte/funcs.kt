package me.clip.voteparty.exte

import co.aikar.commands.ACFUtil
import co.aikar.commands.CommandIssuer
import co.aikar.locales.MessageKeyProvider
import me.clip.placeholderapi.PlaceholderAPI
import me.clip.voteparty.base.Addon
import me.clip.voteparty.conf.objects.Command
import me.clip.voteparty.conf.sections.PluginSettings
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable

private val serializer = LegacyComponentSerializer.legacyAmpersand()

internal fun color(message: String): String
{
	return ChatColor.translateAlternateColorCodes('&', message)
}

internal fun formMessage(player: OfflinePlayer, message: String): String
{
	return color(PlaceholderAPI.setPlaceholders(player, message))
}

internal fun Addon.sendMessage(receiver: CommandIssuer, message: MessageKeyProvider, placeholderTarget: OfflinePlayer? = null, vararg replacements: Any = emptyArray())
{
	var msg = receiver.manager.locales.getMessage(receiver, message)
	
	if (replacements.isNotEmpty() && replacements.size % 2 == 0)
	{
		msg = ACFUtil.replaceStrings(msg, *replacements.map(Any::toString).toTypedArray())
	}
	
	val result = formMessage(Bukkit.getOfflinePlayer(placeholderTarget?.uniqueId ?: receiver.uniqueId), (party.conf().getProperty(PluginSettings.PREFIX) ?: PREFIX) + msg)
	
	party.audiences().sender(receiver.getIssuer()).sendMessage(Identity.nil(), serializer.deserialize(result))
}

internal fun msgAsString(issuer: CommandIssuer, key: MessageKeyProvider): String
{
	return issuer.manager.locales.getMessage(issuer, key)
}


internal fun Plugin.runTaskTimer(period: Long, task: BukkitRunnable.() -> Unit)
{
	object : BukkitRunnable()
	{
		override fun run()
		{
			task.invoke(this)
		}
	}.runTaskTimer(this, 0L, period)
}

internal fun Plugin.runTaskTimerAsync(period: Long, task: BukkitRunnable.() -> Unit)
{
	object : BukkitRunnable()
	{
		override fun run()
		{
			task.invoke(this)
		}
	}.runTaskTimerAsynchronously(this, 0L, period)
}

internal fun Plugin.runTaskLater(delay: Long, task: () -> Unit)
{
	server.scheduler.runTaskLater(this, task, delay)
}


internal fun Collection<Command>.takeRandomly(amount: Int): Collection<Command>
{
	return filter(Command::shouldExecute).shuffled().take(amount)
}


internal fun item(type: Material, amount: Int, function: ItemMeta.() -> Unit = {}): ItemStack
{
	return ItemStack(type, amount).meta(function)
}

internal fun ItemStack.meta(function: ItemMeta.() -> Unit): ItemStack
{
	itemMeta = itemMeta?.apply(function)
	
	return this
}

internal var ItemMeta.name: String
	get() = displayName
	set(value)
	{
		setDisplayName(color(value))
	}
