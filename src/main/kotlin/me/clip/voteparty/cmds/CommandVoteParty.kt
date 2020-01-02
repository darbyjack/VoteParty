package me.clip.voteparty.cmds

import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandHelp
import co.aikar.commands.CommandIssuer
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.HelpCommand
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import co.aikar.commands.annotation.Values
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import me.clip.voteparty.VoteParty
import me.clip.voteparty.base.ADMIN_PERM
import me.clip.voteparty.base.Addon
import me.clip.voteparty.base.sendMessage
import me.clip.voteparty.messages.Messages

@CommandAlias("vp")
data class CommandVoteParty(private val voteParty: VoteParty) : BaseCommand(), Addon
{
	
	override val plugin = voteParty.plugin
	
	
	@Subcommand("addvote")
	@Description("{@@descriptions.add-vote}")
	@Syntax("<amount>")
	@CommandPermission(ADMIN_PERM)
	fun addVote(issuer: CommandIssuer, @Default("1") amount: Int)
	{
		if (amount <= 0) {
			// Throw a hissy fit
		}
		else {
			party.votesHandler.addVote(amount)
			// Send message saying X amount of votes have been added. The total amount of votes is now Y
		}
	}
	
	@Subcommand("givecrate")
	@Description("{@@descriptions.give-crate}")
	@CommandCompletion("@players")
	@Syntax("<player> <amount>")
	@CommandPermission(ADMIN_PERM)
	fun giveCrate(issuer: CommandIssuer, @Values("@players") target: OnlinePlayer, @Default("1") amount: Int)
	{
		// Handle giving a crate to a player
	}
	
	@Subcommand("setcounter")
	@Description("{@@descriptions.set-counter}")
	@Syntax("<amount>")
	@CommandPermission(ADMIN_PERM)
	fun setCounter(issuer: CommandIssuer, amount: Int)
	{
		if (amount <= 0) {
			// Throw a hissy fit
		}
		else {
			party.conf().party?.votesNeeded = amount
			// Send message saying the new amount of votes needed is now X
		}
	}
	
	@Subcommand("startparty")
	@Description("{@@descriptions.start-party}")
	@CommandPermission(ADMIN_PERM)
	fun startParty(issuer: CommandIssuer)
	{
		party.partyHandler.startParty()
		sendMessage(issuer, Messages.PARTY__FORCE_START_SUCCESSFUL)
	}
	
	@Subcommand("giveparty")
	@Description("{@@descriptions.give-party}")
	@CommandCompletion("@players")
	@Syntax("<player>")
	@CommandPermission(ADMIN_PERM)
	fun giveParty(issuer: CommandIssuer, @Values("@players") target: OnlinePlayer)
	{
		voteParty.partyHandler.giveRandomPartyRewards(target.player)
	}
	
	@HelpCommand
	@Description("{@@descriptions.help}")
	fun help(issuer: CommandIssuer, help: CommandHelp)
	{
		if (issuer.hasPermission(ADMIN_PERM))
		{
			help.showHelp()
		} else
		{
			sendMessage(currentCommandIssuer, Messages.INFO__VOTES_NEEDED)
		}
	}
	
}