package me.clip.voteparty.exte

import co.aikar.commands.ACFUtil
import co.aikar.commands.CommandIssuer
import co.aikar.locales.MessageKeyProvider
import me.clip.placeholderapi.PlaceholderAPI
import me.clip.voteparty.VoteParty
import me.clip.voteparty.base.Addon
import me.clip.voteparty.conf.objects.Command
import me.clip.voteparty.conf.sections.PluginSettings
import me.clip.voteparty.conf.sections.VoteSettings
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.title.Title
import net.kyori.adventure.util.Ticks
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

internal fun deserialize(message: String): Component
{
	return serializer.deserialize(color(message))
}

internal fun formMessage(player: OfflinePlayer, message: String): String
{
	return color(PlaceholderAPI.setPlaceholders(player, message))
}

private fun parseMessage(party: VoteParty, receiver: CommandIssuer, message: MessageKeyProvider, placeholderTarget: OfflinePlayer? = null, vararg replacements: Any = emptyArray()): String
{
	var msg = receiver.manager.locales.getMessage(receiver, message)

	if (replacements.isNotEmpty() && replacements.size % 2 == 0)
	{
		msg = ACFUtil.replaceStrings(msg, *replacements.map(Any::toString).toTypedArray())
	}

	return formMessage(Bukkit.getOfflinePlayer(placeholderTarget?.uniqueId ?: receiver.uniqueId), (party.conf().getProperty(PluginSettings.PREFIX) ?: PREFIX) + msg)
}

internal fun Addon.sendActionBar(receiver: CommandIssuer, message: MessageKeyProvider, placeholderTarget: OfflinePlayer? = null, vararg replacements: Any = emptyArray())
{
	val msg = parseMessage(party, receiver, message, placeholderTarget, *replacements)
	party.audiences().sender(receiver.getIssuer()).sendActionBar(serializer.deserialize(msg))
}

internal fun Addon.sendTitle(receiver: CommandIssuer, title: MessageKeyProvider, subtitle: MessageKeyProvider, placeholderTarget: OfflinePlayer? = null, vararg replacements: Any = emptyArray())
{
	val titleMsg = parseMessage(party, receiver, title, placeholderTarget, *replacements)
	val subtitleMsg = parseMessage(party, receiver, subtitle, placeholderTarget, *replacements)
	val fadeIn = Ticks.duration(party.conf().getProperty(VoteSettings.REMINDER_TITLE_FADE_IN).toLong())
	val stay = Ticks.duration(party.conf().getProperty(VoteSettings.REMINDER_TITLE_STAY).toLong())
	val fadeOut = Ticks.duration(party.conf().getProperty(VoteSettings.REMINDER_TITLE_FADE_OUT).toLong())
	val times = Title.Times.times(fadeIn, stay, fadeOut)
	party.audiences().sender(receiver.getIssuer()).showTitle(Title.title(serializer.deserialize(titleMsg), serializer.deserialize(subtitleMsg), times))
}

internal fun Addon.sendBossBar(receiver: CommandIssuer, message: MessageKeyProvider, placeholderTarget: OfflinePlayer? = null, vararg replacements: Any = emptyArray())
{
	val msg = parseMessage(party, receiver, message, placeholderTarget, *replacements)
	val progress = getZeroToOne(party.conf().getProperty(VoteSettings.REMINDER_BOSSBAR_FILL))
	val color = BossBar.Color.NAMES.valueOr(party.conf().getProperty(VoteSettings.REMINDER_BOSSBAR_COLOR).lowercase(), BossBar.Color.PURPLE)
	val overlay = BossBar.Overlay.NAMES.valueOr(party.conf().getProperty(VoteSettings.REMINDER_BOSSBAR_OVERLAY).lowercase(), BossBar.Overlay.PROGRESS)
	val bossBar = BossBar.bossBar(serializer.deserialize(msg), progress, color, overlay)
	party.audiences().sender(receiver.getIssuer()).showBossBar(bossBar)
	plugin.runTaskLater(party.conf().getProperty(VoteSettings.REMINDER_BOSSBAR_STAY_TIME).toLong()){
		party.audiences().sender(receiver.getIssuer()).hideBossBar(bossBar)
	}
}

internal fun Addon.sendMessage(receiver: CommandIssuer, message: MessageKeyProvider, placeholderTarget: OfflinePlayer? = null, vararg replacements: Any = emptyArray())
{
	val msg = parseMessage(party, receiver, message, placeholderTarget, *replacements)
	party.audiences().sender(receiver.getIssuer()).sendMessage(Identity.nil(), serializer.deserialize(msg))
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

private fun getZeroToOne(value: Double): Float
{
	return when
	{
		value < 0.0 -> 0.0f
		value > 1.0 -> 1.0f
		else        -> value.toFloat()
	}
}