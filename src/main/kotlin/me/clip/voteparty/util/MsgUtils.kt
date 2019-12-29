package me.clip.voteparty.util

import co.aikar.commands.CommandIssuer
import co.aikar.commands.CommandManager
import co.aikar.locales.MessageKeyProvider
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit

class MsgUtils
{
	companion object {
		fun sendMessage(manager: CommandManager<*, *, *, *, *, *>, issuer: CommandIssuer, key: MessageKeyProvider)
		{
			var message = manager.getLocales().getMessage(issuer, key.messageKey)
			message = PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(issuer.uniqueId), message)
			issuer.sendMessage(message)
		}
	}
}