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

fun hover(properName: String, displayUsage: String, args: String, permission: String, description: Messages, manager: CommandManager<*, *, *, *, *, *>, sender: CommandSender): Component
{
	val line = TextComponent.newline()
	val desc = manager.getLocales().getMessage(manager.getCommandIssuer(sender), description.messageKey)
	return TextComponent.builder()
			.append(bold(properName, true).color(TextColor.LIGHT_PURPLE))
			.append(line)
			.append(bold(desc, false).color(TextColor.WHITE))
			.append(line)
			.append(line)
			.append(bold(displayUsage, false).color(TextColor.GRAY))
			.append(bold(args, false).color(TextColor.WHITE))
			.append(line)
			.append(bold(permission, false).color(TextColor.DARK_GRAY))
			.build()
}

fun display(sender: CommandSender, manager: CommandManager<*, *, *, *, *, *>)
{
	val line = TextComponent.newline()
	val menu = TextComponent.builder()
			.append(header())
			.append(line)
			.append(show(create("addvote"), hover("Add Vote", "/vp addvote ", "<amount>", ADMIN_PERM, Messages.DESCRIPTIONS__ADD_VOTE, manager, sender), "/vp addvote"))
			.append(line)
			.append(show(create("givecrate"), hover("Give Crate", "/vp givecreate ", "<player> <amount>", ADMIN_PERM, Messages.DESCRIPTIONS__GIVE_CRATE, manager, sender), "/vp givecrate"))
			.append(line)
			.append(show(create("giveparty"), hover("Give Party", "/vp givecreate ", "<player>", ADMIN_PERM, Messages.DESCRIPTIONS__GIVE_PARTY, manager, sender), "/vp giveparty"))
			.append(line)
			.append(show(create("help"), hover("Help", "/vp help ", "", "", Messages.DESCRIPTIONS__HELP, manager, sender), "/vp help"))
			.append(line)
			.append(show(create("reload"), hover("Reload", "/vp reload ", "", ADMIN_PERM, Messages.DESCRIPTIONS__RELOAD, manager, sender), "/vp reload"))
			.append(line)
			.append(show(create("setcounter"), hover("Set Counter", "/vp setcounter ", "<amount>", ADMIN_PERM, Messages.DESCRIPTIONS__SET_COUNTER, manager, sender), "/vp setcounter"))
			.append(line)
			.append(show(create("startparty"), hover("Start Party", "/vp startparty ", "", ADMIN_PERM, Messages.DESCRIPTIONS__START_PARTY, manager, sender), "/vp startparty"))
			.append(line)
			.append(footer())
			.build()
	
	TextAdapter.sendComponent(sender, menu)
}
