package me.clip.voteparty.exte

import co.aikar.commands.CommandIssuer
import me.clip.voteparty.messages.Messages
import me.clip.voteparty.user.RecentVoters
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import java.util.*

fun helpMenu(issuer: CommandIssuer): Component
{
	val mini = MiniMessage.miniMessage()
	val builder = Component.text()
	val lineText = msgAsString(issuer, Messages.HELP__LINE_TEXT)
	val lineHoverTemplate = msgAsString(issuer, Messages.HELP__LINE_HOVER)

	// Header
	builder.append(mini.deserialize(msgAsString(issuer, Messages.HELP__HEADER))).append(Component.newline())

	// Commands
	issuer.manager.getRootCommand("vp").subCommands.entries().forEach {
		val key = it.key
		val cmd = it.value
		if (key != "__default")
		{
			val updatedHover = lineHoverTemplate
					.replace("{text}", cmd.helpText)
					.replace("{desc}", msgAsString(issuer, Messages.valueOf("DESCRIPTIONS__" + key.uppercase(Locale.getDefault()))))
					.replace("{cmd}", it.key)
					.replace("{args}", cmd.syntaxText)
					.replace("{perm}", if (key == "help") "" else ADMIN_PERM)

			builder.append(mini.deserialize(lineText.replace("{cmd}", it.key).replace("{line-hover}", updatedHover)))
		}
	}

	// Footer
	builder.append((mini.deserialize(msgAsString(issuer, Messages.HELP__FOOTER))))

	return builder.build()
}

fun recentMenu(issuer: CommandIssuer, recentVoters: RecentVoters): Component
{
	val mini = MiniMessage.miniMessage()
	val builder = Component.text()
	val lineText = msgAsString(issuer, Messages.INFO__RECENT_VOTERS_LINE)

	// Header
	builder.append(mini.deserialize(msgAsString(issuer, Messages.INFO__RECENT_VOTERS_HEADING))).append(Component.newline())

	// Data
	val currentTime = System.currentTimeMillis()

	for ((_, userTime) in recentVoters.voters()) {
		val user = userTime.user
		val time = userTime.time
		val timeDiff = currentTime - time
		val timeDiffHours = timeDiff / (100.0 * 60.0 * 60.0)

		val name = user.name

		var userStr = name

		if (name.trim() == "") {
			userStr = user.uuid.toString()
		}

		val updatedLine = lineText.replace("{user}", userStr)
				.replace("{time}", "%.2f".format(timeDiffHours))
		builder.append(mini.deserialize(updatedLine))
	}

	return builder.build()
}