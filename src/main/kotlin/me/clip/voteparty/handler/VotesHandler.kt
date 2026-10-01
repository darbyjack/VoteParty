package me.clip.voteparty.handler

import me.clip.voteparty.base.Addon
import me.clip.voteparty.base.State
import me.clip.voteparty.conf.objects.CumulativeVoteRewards
import me.clip.voteparty.conf.objects.CumulativeVoting
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
import me.clip.voteparty.user.User
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
	 * Records the cumulative reward thresholds the vote just now recorded has reached.
	 *
	 * Called for every vote, whether or not the player is online or has room for anything, because
	 * a threshold reached by a vote that cannot be paid where it was cast still has to go out later.
	 * A player who crosses a daily threshold at 23:59 while offline has it waiting at 00:01.
	 */
	fun queueCrossedCumulativeRewards(user: User)
	{
		val settings = party.conf().getProperty(VoteSettings.CUMULATIVE_VOTE_REWARDS)
		
		for ((period, rewards) in enabledPeriods(settings))
		{
			user.queueCrossedCumulativeRewards(period, periodStart(period), rewards.entries)
		}
	}
	
	/**
	 * Hands over every cumulative reward [player] has reached and has not been handed over yet.
	 *
	 * Runs on the vote, on login and on a claim, which are the three points at which there is
	 * somebody there to receive something. A full inventory defers, the same way the vote's own
	 * rewards defer, and for the same reason: the commands run from the console either way, so a
	 * `give` would only put the items on the floor.
	 *
	 * A waiting reward is dropped once its commands have gone to the server, so a threshold reached
	 * once is handed over once on each run that gets to it. That is not a transactional promise:
	 * a command that throws leaves its reward waiting and is tried again next time, repeating the
	 * commands that already ran, and so does a crash between the dispatch and the save. A reward
	 * arriving twice is the cheaper of the two failures, which is why the drop is last.
	 */
	fun giveCumulativeRewards(player: Player)
	{
		giveCumulativeRewards(player, LeaderboardType.values.toSet())
	}
	
	@Deprecated("Pays everything the player is owed. Kept working for anything still calling it; use giveCumulativeRewards.", ReplaceWith("giveCumulativeRewards(player)"))
	fun checkDailyCumulative(player: Player)
	{
		giveCumulativeRewards(player, setOf(LeaderboardType.DAILY))
	}
	
	@Deprecated("Pays everything the player is owed. Kept working for anything still calling it; use giveCumulativeRewards.", ReplaceWith("giveCumulativeRewards(player)"))
	fun checkWeeklyCumulative(player: Player)
	{
		giveCumulativeRewards(player, setOf(LeaderboardType.WEEKLY))
	}
	
	@Deprecated("Pays everything the player is owed. Kept working for anything still calling it; use giveCumulativeRewards.", ReplaceWith("giveCumulativeRewards(player)"))
	fun checkMonthlyCumulative(player: Player)
	{
		giveCumulativeRewards(player, setOf(LeaderboardType.MONTHLY))
	}
	
	@Deprecated("Pays everything the player is owed. Kept working for anything still calling it; use giveCumulativeRewards.", ReplaceWith("giveCumulativeRewards(player)"))
	fun checkYearlyCumulative(player: Player)
	{
		giveCumulativeRewards(player, setOf(LeaderboardType.ANNUALLY))
	}
	
	@Deprecated("Pays everything the player is owed. Kept working for anything still calling it; use giveCumulativeRewards.", ReplaceWith("giveCumulativeRewards(player)"))
	fun checkTotalCumulative(player: Player)
	{
		giveCumulativeRewards(player, setOf(LeaderboardType.ALLTIME))
	}
	
	private fun giveCumulativeRewards(player: Player, periods: Set<LeaderboardType>)
	{
		if (player.inventory.firstEmpty() == -1 && party.conf().getProperty(VoteSettings.CLAIMABLE_IF_FULL))
		{
			return
		}
		
		val settings = party.conf().getProperty(VoteSettings.CUMULATIVE_VOTE_REWARDS)
		val sections = enabledPeriods(settings)
		val user = party.usersHandler[player]

		// Only the periods currently switched on are drained, so a reward reached while its period
		// was off is still there to go out if the period is turned back on. One whose threshold has
		// been taken out of the config has nothing left to give, and goes with the rest.
		val drained = user.drainCumulativeRewards(periods intersect sections.keys)
		{ reward ->
			sections[reward.period]?.entries?.firstOrNull { it.votes == reward.votes }?.commands?.forEach()
			{ command ->
				server.dispatchCommand(server.consoleSender, formMessage(player, command))
			}
		}
		
		if (drained == 0)
		{
			return
		}
		
		// The vote listener saves before it reaches here, so a payout not saved on its own would be
		// handed over again after a restart whenever saving on vote is on.
		if (party.conf().getProperty(PluginSettings.SAVE_ON_VOTE))
		{
			party.usersHandler.save(user)
		}
	}
	
	/**
	 * The cumulative periods that are switched on and have something in them, against the section
	 * each one reads.
	 */
	private fun enabledPeriods(settings: CumulativeVoting): Map<LeaderboardType, CumulativeVoteRewards>
	{
		return mapOf(
			LeaderboardType.DAILY to settings.daily,
			LeaderboardType.WEEKLY to settings.weekly,
			LeaderboardType.MONTHLY to settings.monthly,
			LeaderboardType.ANNUALLY to settings.yearly,
			LeaderboardType.ALLTIME to settings.total
		).filterValues { it.enabled && it.entries.isNotEmpty() }
	}
	
	private fun periodStart(period: LeaderboardType): Long
	{
		return period.start.invoke().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
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