package me.clip.voteparty.conf.sections

import ch.jalu.configme.Comment
import ch.jalu.configme.SettingsHolder
import ch.jalu.configme.properties.BeanProperty
import ch.jalu.configme.properties.Property
import ch.jalu.configme.properties.PropertyInitializer.*
import me.clip.voteparty.conf.mapper.SingleValueToCollectionMapper
import me.clip.voteparty.conf.objects.Command
import me.clip.voteparty.conf.objects.Commands
import me.clip.voteparty.conf.objects.PermCommands
import me.clip.voteparty.conf.objects.PermRewards
import me.clip.voteparty.conf.objects.RewardsPerEvent

internal object PartySettings : SettingsHolder
{

	@JvmField
	@Comment("The amount of votes needed for a party to occur")
	val VOTES_NEEDED: Property<Int> = newProperty("party.votes_needed", 50)

	@JvmField
	@Comment("Would you like to use a crate for the rewards?")
	val USE_CRATE: Property<Boolean> = newProperty("party.use_crate", false)

	@JvmField
	@Comment("The list of worlds where party rewards won't be given")
	val DISABLED_WORLDS: Property<List<String>> = newListProperty("party.disabled_worlds", "")

	@JvmField
	@Comment("Choose to allow offline votes count towards the party")
	val OFFLINE_VOTES: Property<Boolean> = newProperty("party.offline_votes", true)

	@JvmField
	@Comment(
		"There are 3 different ways that a party can work.",
		"1) \"everyone\" - everyone can join the party whether they voted or not",
		"2) \"daily\" - everyone who voted in the past 24 hours can join the party",
		"3) \"party\" - everyone who voted in this specific party can join the party",
		"Set the mode below to one of the options above to specify how the party should function"
	)
	val PARTY_MODE: Property<String> = newProperty("party.party_mode", "everyone")

	@JvmField
	@Comment("The amount of time (in seconds) the server will wait to start the party after the amount needed has been achieved")
	val START_DELAY: Property<Int> = newProperty("party.start_delay", 15)

	@JvmField
	@Comment("The amount of time (in seconds) the server will wait between executing reward commands")
	val COMMAND_DELAY: Property<Int> = newProperty("party.command_delay", 1)

	@JvmField
	@Comment("Configuration for chance rewards to be given during a party.", "Add as many commands as you want, set their chance, and choose the max amount a player can earn!")
	val REWARD_COMMANDS: Property<RewardsPerEvent> = BeanProperty(RewardsPerEvent::class.java, "party.reward_commands", RewardsPerEvent(true, 1, listOf(Command(50.0, listOf("eco give %player_name% 100")), Command(50.0, listOf("give %player_name% DIAMOND 6")), Command(50.0, listOf("give %player_name% IRON_INGOT 12")))), SingleValueToCollectionMapper())

	@JvmField
	@Comment("Configuration for extra commands to be executed on players who have specific permission nodes when a party happens")
	val PERMISSION_PARTY_REWARDS: Property<PermRewards> = newBeanProperty(PermRewards::class.java, "party.permission-rewards", PermRewards(true, listOf(PermCommands("my.special.permission", listOf("eco give %player_name% 500")))))

	@JvmField
	@Comment("A list of rewards that will ALWAYS be given to a player during a party")
	val GUARANTEED_REWARDS: Property<Commands> = newBeanProperty(Commands::class.java, "party.guaranteed_rewards", Commands(true, listOf("eco give %player_name% 10", "give %player_name% STEAK 8")))

	@JvmField
	@Comment("Commands to be executed before a party is started")
	val PRE_PARTY_COMMANDS: Property<Commands> = newBeanProperty(Commands::class.java, "party.pre_party_commands", Commands(true, listOf("broadcast Party will start soon!")))

	@JvmField
	@Comment("Commands to be executed when a party has started")
	val PARTY_COMMANDS: Property<Commands> = newBeanProperty(Commands::class.java, "party.party_commands", Commands(true, listOf("broadcast A Vote Party has started!")))

}
