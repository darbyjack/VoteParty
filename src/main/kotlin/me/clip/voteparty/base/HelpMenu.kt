package me.clip.voteparty.base

import co.aikar.commands.CommandManager
import me.clip.voteparty.messages.Messages
import net.kyori.text.Component
import net.kyori.text.TextComponent
import net.kyori.text.adapter.bukkit.TextAdapter
import net.kyori.text.event.ClickEvent
import net.kyori.text.event.HoverEvent
import net.kyori.text.format.TextColor
import net.kyori.text.format.TextDecoration
import org.bukkit.command.CommandSender

//TODO Clean this stuff up later

fun strike(content: String, apply: Boolean): TextComponent
{
	return TextComponent.builder().content(content).decoration(TextDecoration.STRIKETHROUGH, apply).build()
}

fun bold(content: String, apply: Boolean): TextComponent
{
	return TextComponent.builder().content(content).decoration(TextDecoration.BOLD, apply).build()
}

fun header(): Component
{
	val lines = strike("     ", true).color(TextColor.DARK_PURPLE)
	return TextComponent.builder()
			.append(lines)
			.append(strike(" VoteParty Help ", false).color(TextColor.LIGHT_PURPLE))
			.append(lines)
			.build()
}

fun footer(): Component
{
	return TextComponent.builder()
			.append(strike("                               ", true).color(TextColor.DARK_PURPLE))
			.append(TextComponent.newline().decoration(TextDecoration.STRIKETHROUGH, false))
			.append(strike("Hover for more info", false).color(TextColor.GRAY).decoration(TextDecoration.ITALIC, true))
			.build()
}

fun create(name: String): Component
{
	return TextComponent.builder()
			.content("• ").color(TextColor.DARK_GRAY)
			.append(TextComponent.of("/vp ").color(TextColor.LIGHT_PURPLE))
			.append(TextComponent.of(name).color(TextColor.WHITE))
			.build()
}

fun show(command: Component, hover: Component, click: String): Component
{
	var cmd = command.hoverEvent(HoverEvent.showText(hover))
	cmd = cmd.clickEvent(ClickEvent.suggestCommand(click))
	return cmd
}

fun hover(name: String, usage: String, args: String, perm: String, description: Messages, manager: CommandManager<*, *, *, *, *, *>, sender: CommandSender): Component
{
	val line = TextComponent.newline()
	val desc = manager.getLocales().getMessage(manager.getCommandIssuer(sender), description.messageKey)
	return TextComponent.builder()
			.append(bold(name, true).color(TextColor.LIGHT_PURPLE))
			.append(line)
			.append(bold(desc, false).color(TextColor.WHITE))
			.append(line)
			.append(line)
			.append(bold(usage, false).color(TextColor.GRAY))
			.append(bold(args, false).color(TextColor.WHITE))
			.append(line)
			.append(bold(perm, false).color(TextColor.DARK_GRAY))
			.build()
}

fun display(sender: CommandSender, manager: CommandManager<*, *, *, *, *, *>)
{
	val line = TextComponent.newline()
	val menu = TextComponent.builder().append(header()).append(line)
	manager.getRootCommand("vp").subCommands.entries().forEach {
		val key = it.key
		val cmd = it.value
		if (key != "__default")
		{
			menu.append(show(create(key), hover(cmd.getHelpText(), "/vp $key ", cmd.getSyntaxText(), if (key == "help") "" else ADMIN_PERM, Messages.valueOf("DESCRIPTIONS__" + key.toUpperCase()), manager, sender), "/vp $key"))
			menu.append(line)
		}
	}
	menu.append(footer())
	TextAdapter.sendComponent(sender, menu.build())
}
