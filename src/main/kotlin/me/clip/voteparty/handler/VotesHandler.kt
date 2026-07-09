package me.clip.voteparty.handler

import me.clip.voteparty.base.Addon
import me.clip.voteparty.base.State
import me.clip.voteparty.conf.sections.EffectsSettings
import me.clip.voteparty.conf.sections.PartySettings
import me.clip.voteparty.conf.sections.VoteData
import me.clip.voteparty.conf.sections.VoteSettings
import me.clip.voteparty.exte.formMessage
import me.clip.voteparty.exte.sendActionBar
import me.clip.voteparty.exte.sendBossBar
import me.clip.voteparty.exte.sendMessage
import me.clip.voteparty.exte.sendTitle
import me.clip.voteparty.exte.takeRandomly
import me.clip.voteparty.leaderboard.LeaderboardType
import me.clip.voteparty.messages.Messages
import me.clip.voteparty.plugin.VotePartyPlugin
import me.clip.voteparty.version.EffectType
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class VotesHandler(override val plugin: VotePartyPlugin) : Addon, State
{
	
	private val votes = AtomicInteger()
	
	
	override fun load()
	{
		votes.set(party.voteData().getProperty(VoteData.COUNTER))
	}
	
	override fun kill()
	{
		votes.set(0)
	}
	
	
	fun getVotes(): Int
	{
		return votes.get()
	}
	
	fun setVotes(amount: Int)
	{
		votes.set(amount)
	}
	
	fun addVotes(amount: Int)
	{
		if (votes.addAndGet(amount) < party.conf().getProperty(PartySettings.VOTES_NEEDED))
		{
			return
		}
		
		votes.set(0)
		party.partyHandler.startParty()
	}
	
	fun runAll(player: Player)
	{
		giveRandomVoteRewards(player)
		giveGuaranteedVoteRewards(player)
		givePermissionVoteRewards(player)
	}
	
	
	fun giveGuaranteedVoteRewards(player: Player)
	{
		val settings = party.conf().getProperty(VoteSettings.GUARANTEED_REWARDS)
		
		if (!settings.enabled || settings.commands.isEmpty())
		{
			return
		}
		
		settings.commands.forEach()
		{ command ->
			server.dispatchCommand(server.consoleSender, formMessage(player, command))
		}
	}
	
	fun givePermissionVoteRewards(player: Player)
	{
		val settings = party.conf().getProperty(VoteSettings.PERMISSION_VOTE_REWARDS)
		
		if (!settings.enabled || settings.permCommands.isEmpty())
		{
			return
		}
		
		settings.permCommands.filter { player.hasPermission(it.permission) }.forEach()
		{ perm ->
			perm.commands.forEach()
			{ command ->
				server.dispatchCommand(server.consoleSender, formMessage(player, command))
			}
		}
	}

	fun checkDailyCumulative(player: Player)
	{
		val settings = party.conf().getProperty(VoteSettings.CUMULATIVE_VOTE_REWARDS)

		if (!settings.daily.enabled || settings.daily.entries.isEmpty())
		{
			return
		}

		settings.daily.entries.filter { entry -> entry.votes == party.usersHandler.getVoteCountSince(player, LeaderboardType.DAILY.start.invoke()) }.forEach { entry ->
			entry.commands.forEach()
			{ command ->
				server.dispatchCommand(server.consoleSender, formMessage(player, command))
			}
		}
	}

	fun checkWeeklyCumulative(player: Player)
	{
		val settings = party.conf().getProperty(VoteSettings.CUMULATIVE_VOTE_REWARDS)

		if (!settings.weekly.enabled || settings.weekly.entries.isEmpty())
		{
			return
		}

		settings.weekly.entries.filter { entry -> entry.votes == party.usersHandler.getVoteCountSince(player, LeaderboardType.WEEKLY.start.invoke()) }.forEach { entry ->
			entry.commands.forEach()
			{ command ->
				server.dispatchCommand(server.consoleSender, formMessage(player, command))
			}
		}
	}

	fun checkMonthlyCumulative(player: Player)
	{
		val settings = party.conf().getProperty(VoteSettings.CUMULATIVE_VOTE_REWARDS)

		if (!settings.monthly.enabled || settings.monthly.entries.isEmpty())
		{
			return
		}

		settings.monthly.entries.filter { entry -> entry.votes == party.usersHandler.getVoteCountSince(player, LeaderboardType.MONTHLY.start.invoke()) }.forEach { entry ->
			entry.commands.forEach()
			{ command ->
				server.dispatchCommand(server.consoleSender, formMessage(player, command))
			}
		}
	}

	fun checkYearlyCumulative(player: Player)
	{
		val settings = party.conf().getProperty(VoteSettings.CUMULATIVE_VOTE_REWARDS)

		if (!settings.yearly.enabled || settings.yearly.entries.isEmpty())
		{
			return
		}

		settings.yearly.entries.filter { entry -> entry.votes == party.usersHandler.getVoteCountSince(player, LeaderboardType.ANNUALLY.start.invoke()) }.forEach { entry ->
			entry.commands.forEach()
			{ command ->
				server.dispatchCommand(server.consoleSender, formMessage(player, command))
			}
		}
	}

	fun checkTotalCumulative(player: Player)
	{
		val settings = party.conf().getProperty(VoteSettings.CUMULATIVE_VOTE_REWARDS)

		if (!settings.total.enabled || settings.total.entries.isEmpty())
		{
			return
		}

		settings.total.entries.filter { entry -> entry.votes == party.usersHandler.getVoteCountSince(player, LeaderboardType.ALLTIME.start.invoke()) }.forEach { entry ->
			entry.commands.forEach()
			{ command ->
				server.dispatchCommand(server.consoleSender, formMessage(player, command))
			}
		}
	}
	
	fun giveVotesiteVoteRewards(player: Player, serviceName: String)
	{
		val settings = party.conf().getProperty(VoteSettings.VOTESITE_VOTE_REWARDS)
		
		if (!settings.enabled || settings.votesiteCommands.isEmpty())
		{
			return
		}
		
		val first = settings.votesiteCommands.firstOrNull { serviceName == it.serviceName }
		
		first?.commands?.forEach()
		{
			server.dispatchCommand(server.consoleSender, formMessage(player, it))
		}
	}
	
	fun giveFirstTimeVoteRewards(player: Player)
	{
		val settings = party.conf().getProperty(VoteSettings.FIRST_TIME_REWARDS)
		
		if (!settings.enabled || settings.commands.isEmpty())
		{
			return
		}
		
		settings.commands.forEach()
		{ command ->
			server.dispatchCommand(server.consoleSender, formMessage(player, command))
		}
	}
	
	fun giveRandomVoteRewards(player: Player)
	{
		val settings = party.conf().getProperty(VoteSettings.PER_VOTE_REWARDS)
		
		if (!settings.enabled || settings.max_possible <= 0 || settings.commands.isEmpty())
		{
			return
		}
		
		settings.commands.takeRandomly(settings.max_possible).forEach()
		{ section ->
			section.command.forEach()
			{ command ->
				server.dispatchCommand(server.consoleSender, formMessage(player, command))
			}
		}
	}
	
	fun playerVoteEffects(player: Player)
	{
		val settings = party.conf().getProperty(EffectsSettings.VOTE)
		
		if (!settings.enable || settings.effects.isEmpty())
		{
			return
		}
		
		val location = player.location
		
		settings.effects.forEach {
			party.hook().display(EffectType.valueOf(it), location, settings.offsetX, settings.offsetY, settings.offsetZ, settings.speed, settings.count)
		}
	}
	
	fun runGlobalCommands(player: Player)
	{
		val settings = party.conf().getProperty(VoteSettings.GLOBAL_COMMANDS)
		
		if (!settings.enabled || settings.commands.isEmpty())
		{
			return
		}
		
		settings.commands.forEach()
		{ command ->
			server.dispatchCommand(server.consoleSender, formMessage(player, command))
		}
	}

	fun sendVoteReminders()
	{
		val players = Bukkit.getOnlinePlayers().filter {
			party.usersHandler.getVoteCountSince(
				it,
				party.conf().getProperty(VoteSettings.REMINDER_INTERVAL).toLong(),
				TimeUnit.HOURS
			) < party.conf().getProperty(VoteSettings.REMINDER_THRESHOLD)
		}

		players.forEach {
			when(party.conf().getProperty(VoteSettings.REMINDER_MESSAGE_TYPE)) {
				"actionbar" -> sendActionBar(party.manager().getCommandIssuer(it), Messages.VOTES__REMINDER)
				"bossbar" -> sendBossBar(party.manager().getCommandIssuer(it), Messages.VOTES__REMINDER)
				"title" -> sendTitle(party.manager().getCommandIssuer(it), Messages.VOTES__REMINDER, Messages.VOTES__REMINDER_SUBTITLE)
				else -> sendMessage(party.manager().getCommandIssuer(it), Messages.VOTES__REMINDER)
			}
		}
	}
	
}