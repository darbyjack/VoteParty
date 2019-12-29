package me.clip.voteparty.base

import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandIssuer
import co.aikar.locales.MessageKeyProvider
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit

fun BaseCommand.sendMessage(issuer: CommandIssuer, key: MessageKeyProvider)
{
	issuer.sendMessage(PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(issuer.uniqueId), currentCommandManager.getLocales().getMessage(issuer, key.messageKey)))
}