package me.clip.voteparty.listener

import me.clip.voteparty.conf.sections.PartySettings
import me.clip.voteparty.conf.sections.PluginSettings
import me.clip.voteparty.conf.sections.VoteSettings
import me.clip.voteparty.events.VoteReceivedEvent
import me.clip.voteparty.exte.runTaskLater
import me.clip.voteparty.exte.sendMessage
import me.clip.voteparty.listener.base.VotePartyListener
import me.clip.voteparty.messages.Messages
import me.clip.voteparty.plugin.VotePartyPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent

internal class VotesListener(override val plugin: VotePartyPlugin) : VotePartyListener
{

	@EventHandler

	fun VoteReceivedEvent.onReceive()
	{

		var first = false

		val user = party.usersHandler[player]
		if (!user.hasVotedBefore()) {
			first = true
		}
		user.voted()

		party.usersHandler.getRecentVoters()?.voted(user, System.currentTimeMillis())

		if (!player.isOnline && party.conf().getProperty(VoteSettings.OFFLINE_VOTE_CLAIMING)) {
			user.claimable++
		}

		if (party.conf().getProperty(PluginSettings.SAVE_ON_VOTE))
		{
			party.usersHandler.save(user)
		}

		if (!player.isOnline && !party.conf().getProperty(PartySettings.OFFLINE_VOTES))
		{
			return
		}

		if ((party.conf().getProperty(PartySettings.PARTY_MODE) == "party") && !party.partyHandler.voted.contains(user.uuid)) {
			party.partyHandler.voted.add(user.uuid)
		}

		party.votesHandler.addVotes(1)

		val online = player.player

		if (online == null && party.conf().getProperty(VoteSettings.OFFLINE_VOTE_GLOBAL_COMMANDS)) {
			party.votesHandler.runGlobalCommandsOffline(player)
			return
		}

		if (online == null) return

		if (online.inventory.firstEmpty() == -1 && party.conf().getProperty(VoteSettings.CLAIMABLE_IF_FULL)) {
			user.claimable++
			return sendMessage(party.manager().getCommandIssuer(online), Messages.VOTES__INVENTORY_FULL)
		}

		party.votesHandler.runGlobalCommands(online)
		party.votesHandler.runAll(online)

		if (vote != "") {
			party.votesHandler.giveVotesiteVoteRewards(online, vote)
		}

		if (first) {
			party.votesHandler.giveFirstTimeVoteRewards(online)
		}

		party.votesHandler.checkDailyCumulative(online)
		party.votesHandler.checkWeeklyCumulative(online)
		party.votesHandler.checkMonthlyCumulative(online)
		party.votesHandler.checkYearlyCumulative(online)
		party.votesHandler.checkTotalCumulative(online)

		party.votesHandler.playerVoteEffects(online)
	}

	@EventHandler(priority = EventPriority.HIGH)
	fun PlayerJoinEvent.onJoin()
	{
		if (!player.hasPlayedBefore())
		{
			return
		}

		if (party.usersHandler[player].claimable > 0 && party.conf().getProperty(VoteSettings.OFFLINE_VOTE_CLAIMING_NOTIFY))
		{
			plugin.runTaskLater(40L)
			{
				sendMessage(party.manager().getCommandIssuer(player), Messages.CLAIM__NOTIFY)
			}
		}
	}

}
