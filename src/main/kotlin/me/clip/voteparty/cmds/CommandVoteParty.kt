package me.clip.voteparty.cmds

import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandHelp
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.HelpCommand
import me.clip.voteparty.base.BASE_PERM

@CommandAlias("vp")
class CommandVoteParty : BaseCommand()
{
	
	@HelpCommand
	@Description("{@@descriptions.help}")
	@CommandPermission(BASE_PERM + "help")
	fun help(help: CommandHelp)
	{
		help.showHelp()
	}
	
}