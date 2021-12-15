package me.clip.voteparty.conf.sections

import ch.jalu.configme.Comment
import ch.jalu.configme.SettingsHolder
import ch.jalu.configme.properties.BeanProperty
import ch.jalu.configme.properties.Property
import ch.jalu.configme.properties.PropertyInitializer.newBeanProperty
import ch.jalu.configme.properties.PropertyInitializer.newProperty
import me.clip.voteparty.conf.mapper.SingleValueToCollectionMapper
import me.clip.voteparty.conf.objects.Command
import me.clip.voteparty.conf.objects.Commands
import me.clip.voteparty.conf.objects.CumulativeVoting
import me.clip.voteparty.conf.objects.PermCommands
import me.clip.voteparty.conf.objects.PermRewards
import me.clip.voteparty.conf.objects.RewardsPerEvent
import me.clip.voteparty.conf.objects.VotesiteCommands
import me.clip.voteparty.conf.objects.VotesiteRewards

internal object VoteSettings : SettingsHolder
{

	@JvmField
	@Comment("If a player's inventory is full when voting, do you want to send the vote to a /vote claim?")
	val CLAIMABLE_IF_FULL: Property<Boolean> = newProperty("voting.claim_if_full", true)

	@JvmField
	@Comment("Configuration for chance rewards to be given for voting.", "Add as many commands as you want, set their chance, and choose the max amount a player can earn!")
	val PER_VOTE_REWARDS: Property<RewardsPerEvent> = BeanProperty(RewardsPerEvent::class.java, "voting.per_vote_rewards", RewardsPerEvent(true, 1, listOf(Command(50.0, listOf("eco give %player_name% 100")), Command(70.0, listOf("give %player_name% STEAK 10")))), SingleValueToCollectionMapper())

	@JvmField
	@Comment("Configuration for extra commands to be executed on players who have specific permission nodes")
	val PERMISSION_VOTE_REWARDS: Property<PermRewards> = newBeanProperty(PermRewards::class.java, "voting.permission-rewards", PermRewards(true, listOf(PermCommands("my.special.permission", listOf("eco give %player_name% 500")))))

	@JvmField
	@Comment("A list of rewards that will ALWAYS be given to a player for voting")
	val GUARANTEED_REWARDS: Property<Commands> = newBeanProperty(Commands::class.java, "voting.guaranteed_rewards", Commands(true, listOf("eco give %player_name% 10", "give %player_name% STEAK 8")))

	@JvmField
	@Comment("A list of commands to run when it's the first time a player has voted (only works for online players)")
	val FIRST_TIME_REWARDS: Property<Commands> = newBeanProperty(Commands::class.java, "voting.first_time_rewards", Commands(false, listOf("eco give %player_name% 100", "give %player_name% STEAK 10")))

	@JvmField
	@Comment("Configuration for extra commands to be executed on players who voted on a specific website (only works for online players)",
	         "Known Service Names:",
	         "TopG.com",
	         "PlanetMinecraft.com",
	         "Minecraft-MP.com",
	         "MinecraftServers.org",
	         "Minecraft-Server.net")
	val VOTESITE_VOTE_REWARDS: Property<VotesiteRewards> = newBeanProperty(VotesiteRewards::class.java, "voting.votesite-rewards", VotesiteRewards(false, listOf(VotesiteCommands("TestVote", listOf("eco give %player_name% 500")))))

	@JvmField
	@Comment("Global commands (such as a broadcast message) to be executed when a player votes")
	val GLOBAL_COMMANDS: Property<Commands> = newBeanProperty(Commands::class.java, "voting.global_commands", Commands(true, listOf("broadcast %player_name% just voted! Only %voteparty_votes_required_party% more votes until a VoteParty!")))

	@JvmField
	@Comment("Would you like players to be able to claim rewards for offline votes?",
	         "Note: They will only be able to get credit for rewards if you have `offline_votes` enabled")
	val OFFLINE_VOTE_CLAIMING: Property<Boolean> = newProperty("voting.offline_vote_claiming.enabled", false)

	@JvmField
	@Comment("Would you like to notify the player when they login that they have votes to claim?")
	val OFFLINE_VOTE_CLAIMING_NOTIFY: Property<Boolean> = newProperty("voting.offline_vote_claiming.notify", false)

	@JvmField
	@Comment("Would you like global commands to be run when an offline player votes?")
	val OFFLINE_VOTE_GLOBAL_COMMANDS: Property<Boolean> = newProperty("voting.offline_vote_claiming.global_commands", false)

	@JvmField
	@Comment("Configuration for extra commands to be executed on players who have voted a specific amount of times in the past day, week, month, year, and all time.")
	val CUMULATIVE_VOTE_REWARDS: Property<CumulativeVoting> = newBeanProperty(CumulativeVoting::class.java, "voting.cumulative-rewards", CumulativeVoting())

	@JvmField
	@Comment("The number of voters show in the recent command")
	val RECENT_VOTE_COUNT: Property<Int> = newProperty("voting.recent_count", 5)
}
