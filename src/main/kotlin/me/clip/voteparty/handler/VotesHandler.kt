package me.clip.voteparty.handler

import me.clip.voteparty.base.Addon
import me.clip.voteparty.base.State
import me.clip.voteparty.conf.sections.EffectsSettings
import me.clip.voteparty.conf.sections.PartySettings
import me.clip.voteparty.conf.sections.PluginSettings
import me.clip.voteparty.conf.sections.VoteData
import me.clip.voteparty.conf.sections.VoteSettings
import me.clip.voteparty.exte.formMessage
import me.clip.voteparty.exte.sendMessage
import me.clip.voteparty.exte.takeRandomly
import me.clip.voteparty.leaderboard.LeaderboardType
import me.clip.voteparty.messages.Messages
import me.clip.voteparty.plugin.VotePartyPlugin
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.time.ZoneId
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

	/**
	 * Hands over every cumulative reward [player] has reached but has not been paid for yet.
	 *
	 * A cumulative reward is keyed to a vote count rather than to a vote, so each threshold only
	 * ever comes due once and has to be paid the moment the count reaches it. That made it easy
	 * to miss: a vote from a player who was offline, or whose inventory was too full to receive
	 * anything, never got as far as a check at all, and an exact match on the count then stepped
	 * over the threshold for good.
	 *
	 * Paying everything between the votes already settled and the votes the player has now covers
	 * all of that — a threshold crossed while they were away, several crossed at once, and a
	 * period that has rolled over since. Nothing is settled until the rewards have actually been
	 * handed over, so a caller that cannot pay leaves the thresholds owed for the next call: the
	 * player's next vote, their next claim, or their next login.
	 */
	fun giveCumulativeRewards(player: Player)
	{
		// Same deferral the vote's own rewards use. Cumulative rewards are console commands, so
		// they would still run with a full inventory — a `give` would just drop the items at the
		// player's feet instead.
		if (player.inventory.firstEmpty() == -1 && party.conf().getProperty(VoteSettings.CLAIMABLE_IF_FULL))
		{
			return
		}

		val settings = party.conf().getProperty(VoteSettings.CUMULATIVE_VOTE_REWARDS)

		val periods = listOf(
			LeaderboardType.DAILY to settings.daily,
			LeaderboardType.WEEKLY to settings.weekly,
			LeaderboardType.MONTHLY to settings.monthly,
			LeaderboardType.ANNUALLY to settings.yearly,
			LeaderboardType.ALLTIME to settings.total
		)

		val user = party.usersHandler[player]
		var given = false

		for ((period, rewards) in periods)
		{
			if (!rewards.enabled || rewards.entries.isEmpty())
			{
				continue
			}

			val since = period.start.invoke().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

			for (entry in user.dueCumulativeRewards(since, rewards.entries))
			{
				entry.commands.forEach()
				{ command ->
					server.dispatchCommand(server.consoleSender, formMessage(player, command))
				}

				given = true
			}
		}

		if (!given)
		{
			return
		}

		user.settleVotes()

		// The vote listener saves before it reaches here, so a payout that is not saved on its
		// own would be paid a second time after a restart whenever saving on vote is enabled.
		if (party.conf().getProperty(PluginSettings.SAVE_ON_VOTE))
		{
			party.usersHandler.save(user)
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
			party.hook().display(it, location, settings.offsetX, settings.offsetY, settings.offsetZ, settings.speed, settings.count)
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
			sendMessage(party.manager().getCommandIssuer(it), Messages.VOTES__REMINDER)
		}
	}
	
}