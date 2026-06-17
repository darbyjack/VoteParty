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
	@Comment("Would you like to validate the usernames being sent from voting sites?")
	val VALIDATE_NAMES: Property<Boolean> = newProperty("voting.validate_names", false)

	@JvmField
	@Comment("This is the regex for username checking. The default should apply to most accounts, but you are given access to modify in case you need to.")
	val NAME_REGEX: Property<String> = newProperty("voting.name_regex", "^[a-zA-Z0-9_]{2,16}$")

	@JvmField
	@Comment("Enable or disable the voting reminder.")
	val REMINDER_ENABLED: Property<Boolean> = newProperty("voting.reminder_enabled", true)

	@JvmField
	@Comment("This is how long (in hours) the plugin should check that it has been since a user has voted before reminding them to vote.")
	val REMINDER_INTERVAL: Property<Int> = newProperty("voting.reminder_interval", 24)

	@JvmField
	@Comment("This is how many votes the plugin should check that a user has in the last X amount of time (defined above) before reminding them to vote.",
		"For example, if you set the reminder interval to 24 hours, and the reminder threshold to 3, the plugin will check if the user has voted 3 times in the last 24 hours.",
		"If they have, they will not be reminded to vote until they have voted 3 times in the last 24 hours.")
	val REMINDER_THRESHOLD: Property<Int> = newProperty("voting.reminder_threshold", 3)

	@JvmField
	@Comment("How often in seconds should players be reminded to vote? (Default is 10 minutes)",
		"Note: A FULL reboot is required for this to take effect if changed after the plugin has been loaded.")
	val REMINDER_INTERVAL_SECONDS: Property<Int> = newProperty("voting.reminder_interval_seconds", 600)

	@JvmField
	@Comment("What message type to send the reminder as? Options are: chat, actionbar, title, bossbar.",
		"If the value is missing or is invalid, chat is used.")
	val REMINDER_MESSAGE_TYPE: Property<String> = newProperty("voting.reminder_message_type", "chat")

	@JvmField
	@Comment("What percentage of the bossbar should be filled? The value should be between 0.0 and 1.0.",
		"Defaults to 1.0 which represents a full bossbar.", "This is only used if the reminder message type is set to bossbar.")
	val REMINDER_BOSSBAR_FILL: Property<Double> = newProperty("voting.reminder_bossbar_fill", 1.0)

	@JvmField
	@Comment("What color should the bossbar be? Options are: pink, blue, red, green, yellow, purple, white.",
		"Defaults to purple.", "This is only used if the reminder message type is set to bossbar.")
	val REMINDER_BOSSBAR_COLOR: Property<String> = newProperty("voting.reminder_bossbar_color", "purple")

	@JvmField
	@Comment("What overlay should the bossbar have? Options are: progress, notched_6, notched_10, notched_12, notched_20.",
		"Defaults to progress.", "This is only used if the reminder message type is set to bossbar.")
	val REMINDER_BOSSBAR_OVERLAY: Property<String> = newProperty("voting.reminder_bossbar_overlay", "progress")

	@JvmField
	@Comment("How long should the bossbar stay on the screen? (in ticks, 1 second = 20 ticks)",
		"Defaults to 3 seconds.", "This is only used if the reminder message type is set to bossbar.")
	val REMINDER_BOSSBAR_STAY_TIME: Property<Int> = newProperty("voting.reminder_bossbar_stay_time", 60)

	@JvmField
	@Comment("How long should the title take to fade in? (in ticks, 1 second = 20 ticks)",
		"Defaults to half a second.", "This is only used if the reminder message type is set to title.")
	val REMINDER_TITLE_FADE_IN: Property<Int> = newProperty("voting.reminder_title_fade_in", 10)

	@JvmField
	@Comment("How long should the title stay on the screen? (in ticks, 1 second = 20 ticks)",
		"Defaults to 3 and a half seconds.", "This is only used if the reminder message type is set to title.")
	val REMINDER_TITLE_STAY: Property<Int> = newProperty("voting.reminder_title_stay", 70)

	@JvmField
	@Comment("How long should the title take to fade out? (in ticks, 1 second = 20 ticks)",
		"Defaults to a second.", "This is only used if the reminder message type is set to title.")
	val REMINDER_TITLE_FADE_OUT: Property<Int> = newProperty("voting.reminder_title_fade_out", 20)

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
	@Comment("Configuration for extra commands to be executed on players who have voted a specific amount of times in the past day, week, month, year, and all time.")
	val CUMULATIVE_VOTE_REWARDS: Property<CumulativeVoting> = newBeanProperty(CumulativeVoting::class.java, "voting.cumulative-rewards", CumulativeVoting())

}
