package me.clip.voteparty.base

import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandIssuer
import co.aikar.locales.MessageKeyProvider
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer


fun formMessage(player: OfflinePlayer, message: String): String
{
	return ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, message))
}

fun color(message: String): String
{
	return ChatColor.translateAlternateColorCodes('&', message)
}

fun BaseCommand.sendMessage(prefix: String, issuer: CommandIssuer, key: MessageKeyProvider)
{
	issuer.sendMessage(formMessage(Bukkit.getOfflinePlayer(issuer.uniqueId), prefix + currentCommandManager.getLocales().getMessage(issuer, key.messageKey)))
}

fun BaseCommand.sendMessage(prefix: String, issuer: CommandIssuer, key: MessageKeyProvider, target: OfflinePlayer)
{
	issuer.sendMessage(formMessage(Bukkit.getOfflinePlayer(target.uniqueId), prefix + currentCommandManager.getLocales().getMessage(issuer, key.messageKey)))
}