package me.clip.voteparty.exte

import co.aikar.commands.CommandIssuer
import me.clip.voteparty.messages.Messages
import me.clip.voteparty.user.RecentVoters
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import java.util.*

fun helpMenu(issuer: CommandIssuer): Component
{
	val mini = MiniMessage.get()
	val builder = Component.text()
	val lineText = msgAsString(issuer, Messages.HELP__LINE_TEXT)
	val lineHoverTemplate = msgAsString(issuer, Messages.HELP__LINE_HOVER)

	// Header
	builder.append(mini.parse(msgAsString(issuer, Messages.HELP__HEADER))).append(Component.newline())

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

			builder.append(mini.parse(lineText.replace("{cmd}", it.key).replace("{line-hover}", updatedHover)))
		}
	}

	// Footer
	builder.append((mini.parse(msgAsString(issuer, Messages.HELP__FOOTER))))

	return builder.build()
}

fun recentMenu(issuer: CommandIssuer, recentVoters: RecentVoters): Component
{
	val mini = MiniMessage.get()
	val builder = Component.text()
	val lineText = msgAsString(issuer, Messages.INFO__RECENT_VOTERS_LINE)

	// Header
	builder.append(mini.parse(msgAsString(issuer, Messages.INFO__RECENT_VOTERS_HEADING))).append(Component.newline())

	// Data
	val currentTime = System.currentTimeMillis()

	for ((user, time) in recentVoters.voters()) {
		val timeDiff = currentTime - time
		val timeDiffHours = timeDiff / (100.0 * 60.0 * 60.0)

		val updatedLine = lineText.replace("{user}", user.name)
				.replace("{time}", "%.2f".format(timeDiffHours))
		builder.append(mini.parse(updatedLine))
	}

	return builder.build()
}