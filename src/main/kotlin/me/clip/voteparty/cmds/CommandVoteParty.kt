package me.clip.voteparty.cmds

import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandIssuer
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import co.aikar.commands.annotation.Values
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import me.clip.voteparty.base.Addon
import me.clip.voteparty.conf.sections.PartySettings
import me.clip.voteparty.conf.sections.VoteSettings
import me.clip.voteparty.events.VoteReceivedEvent
import me.clip.voteparty.exte.*
import me.clip.voteparty.exte.ADMIN_PERM
import me.clip.voteparty.exte.CLAIM_PERM
import me.clip.voteparty.exte.sendMessage
import me.clip.voteparty.messages.Messages
import me.clip.voteparty.plugin.VotePartyPlugin
import net.kyori.adventure.identity.Identity
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.TimeUnit

@CommandAlias("%vp")
internal class CommandVoteParty(override val plugin: VotePartyPlugin) : BaseCommand(), Addon
{

	@Subcommand("addvote")
	@Syntax("<amount> [player]")
	@Description("Add Vote")
	@CommandPermission(ADMIN_PERM)
	fun addVote(issuer: CommandIssuer, @Default("1") amount: Int, @Optional name: String?)
	{

		if (amount <= 0)
		{
			return sendMessage(issuer, Messages.ERROR__INVALID_NUMBER)
		}

		if (!name.isNullOrEmpty())
		{
			val user = party.usersHandler[name] ?: return sendMessage(issuer, Messages.ERROR__USER_NOT_FOUND)

			repeat(amount) {
				server.pluginManager.callEvent(VoteReceivedEvent(user.player(), ""))
			}

			return sendMessage(issuer, Messages.VOTES__ADDED_TO_PLAYER, user.player(), "{count}", amount)
		}

		party.votesHandler.addVotes(amount)
		sendMessage(issuer, Messages.VOTES__VOTE_COUNTER_UPDATED)
	}

	@Subcommand("givecrate")
	@CommandCompletion("@online")
	@Syntax("<player> <amount>")
	@Description("Give Crate")
	@CommandPermission(ADMIN_PERM)
	fun giveCrate(issuer: CommandIssuer, @Values("@online") target: OnlinePlayer, @Default("1") amount: Int)
	{
		if (amount <= 0)
		{
			return sendMessage(issuer, Messages.ERROR__INVALID_NUMBER)
		}

		sendMessage(issuer, Messages.CRATE__CRATE_GIVEN, target.player)
		sendMessage(currentCommandManager.getCommandIssuer(target.player), Messages.CRATE__CRATE_RECEIVED)

		target.player.inventory.addItem(party.partyHandler.buildCrate(amount))
	}

	@Subcommand("setcounter")
	@Syntax("<amount>")
	@Description("Set Counter")
	@CommandPermission(ADMIN_PERM)
	fun setCounter(issuer: CommandIssuer, amount: Int)
	{
		if (amount < 0)
		{
			return sendMessage(issuer, Messages.ERROR__INVALID_NUMBER)
		}

		party.conf().setProperty(PartySettings.VOTES_NEEDED, amount)
		party.conf().save()

		sendMessage(issuer, Messages.VOTES__VOTES_NEEDED_UPDATED)
	}

	@Subcommand("checkvotes")
	@Syntax("<player> <amount> <timeunit>")
	@CommandCompletion("@online")
	@Description("Check Votes")
	@CommandPermission(ADMIN_PERM)
	fun checkVotes(issuer: CommandIssuer, offlinePlayer: OfflinePlayer, amount: Long, unit: TimeUnit)
	{
		val count = party.usersHandler.getVotesWithinRange(offlinePlayer, amount, unit)
		sendMessage(
			issuer,
			Messages.INFO__PLAYER_CHECK_VOTES,
			offlinePlayer,
			"{count}",
			count,
			"{amount}",
			amount,
			"{unit}",
			unit.toString().lowercase(Locale.getDefault())
		)
	}

	@Subcommand("totalvotes")
	@Syntax("<player>")
	@CommandCompletion("@online")
	@Description("Total Votes")
	@CommandPermission(ADMIN_PERM)
	fun totalVotes(issuer: CommandIssuer, offlinePlayer: OfflinePlayer)
	{
		sendMessage(issuer, Messages.INFO__PLAYER_TOTAL_VOTES, offlinePlayer)
	}

	@Subcommand("resetvotes")
	@Syntax("<player>")
	@CommandCompletion("@online")
	@Description("Reset Votes")
	@CommandPermission(ADMIN_PERM)
	fun resetVotes(issuer: CommandIssuer, offlinePlayer: OfflinePlayer)
	{
		party.usersHandler.reset(offlinePlayer)
		sendMessage(issuer, Messages.INFO__VOTE_COUNT_RESET, offlinePlayer)
	}

	@Subcommand("startparty")
	@Description("Start Party")
	@CommandPermission(ADMIN_PERM)
	fun startParty(issuer: CommandIssuer)
	{
		party.partyHandler.startParty()
		sendMessage(issuer, Messages.PARTY__FORCE_START_SUCCESSFUL)
	}

	@Subcommand("giveparty")
	@CommandCompletion("@players")
	@Description("Give Party")
	@Syntax("<player>")
	@CommandPermission(ADMIN_PERM)
	fun giveParty(issuer: CommandIssuer, @Values("@players") target: OnlinePlayer)
	{
		if (target.player.world.name in party.conf().getProperty(PartySettings.DISABLED_WORLDS))
		{
			return sendMessage(issuer, Messages.ERROR__DISABLED_WORLD)
		}

		if (party.conf().getProperty(PartySettings.USE_CRATE))
		{
			target.player.inventory.addItem(party.partyHandler.buildCrate(1))
		}
		else
		{
			party.partyHandler.runAll(target.player)
		}

		sendMessage(issuer, Messages.VOTES__PRIVATE_PARTY_GIVEN, target.player)
		sendMessage(currentCommandManager.getCommandIssuer(target.player), Messages.VOTES__PRIVATE_PARTY_RECEIVED)
	}

	@Subcommand("reload")
	@Description("Reload")
	@CommandPermission(ADMIN_PERM)
	fun reload(issuer: CommandIssuer)
	{
		party.conf().reload()
		party.loadLang()

		sendMessage(issuer, Messages.INFO__RELOADED)
	}

	@Subcommand("claim")
	@Description("Claim")
	@CommandPermission(CLAIM_PERM)
	fun claim(player: Player)
	{
		val user = party.usersHandler[player]

		if (user.claimable <= 0)
		{
			return sendMessage(currentCommandIssuer, Messages.CLAIM__NONE)
		}

		if (player.inventory.firstEmpty() == -1 && party.conf().getProperty(VoteSettings.CLAIMABLE_IF_FULL))
		{
			return sendMessage(currentCommandIssuer, Messages.CLAIM__FULL)
		}

		party.votesHandler.runAll(player)
		user.claimable--

		sendMessage(currentCommandIssuer, Messages.CLAIM__SUCCESS, null, "{claim}", user.claimable)
	}

	@Subcommand("claimall")
	@Description("Claim All")
	@CommandPermission(CLAIM_PERM)
	fun claimAll(player: Player)
	{
		val user = party.usersHandler[player]

		if (user.claimable <= 0)
		{
			return sendMessage(currentCommandIssuer, Messages.CLAIM__NONE)
		}

		for (i in 1..user.claimable)
		{
			if (player.inventory.firstEmpty() == -1 && party.conf().getProperty(VoteSettings.CLAIMABLE_IF_FULL))
			{
				return sendMessage(currentCommandIssuer, Messages.CLAIM__FULL_ALL, null, "{claimed}", i, "{claim}", user.claimable)

			}

			party.votesHandler.runAll(player)
			user.claimable--
		}

		sendMessage(currentCommandIssuer, Messages.CLAIM__SUCCESS_ALL)
	}

	@Subcommand("recent")
	@Description("Show recent voters")
	@CommandPermission("voteparty.recent")
	fun recent(issuer: CommandIssuer) {
		val recentVoters = party.usersHandler.getRecentVoters()

		if (recentVoters == null) {
			sendMessage(issuer, Messages.INFO__RECENT_VOTERS_NULL)
			return
		}

		party.audiences().sender(issuer.getIssuer()).sendMessage(Identity.nil(), recentMenu(issuer, recentVoters))
	}

	@Subcommand("help")
	@Description("Help")
	@CommandPermission("voteparty.help")
	fun help(issuer: CommandIssuer)
	{
		party.audiences().sender(issuer.getIssuer()).sendMessage(Identity.nil(), helpMenu(issuer))
	}

	@Default
	fun default(issuer: CommandIssuer)
	{
		sendMessage(issuer, Messages.INFO__VOTES_NEEDED)
	}

}