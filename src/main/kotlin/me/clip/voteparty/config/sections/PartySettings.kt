package me.clip.voteparty.config.sections

import ch.jalu.configme.SettingsHolder
import ch.jalu.configme.properties.Property
import ch.jalu.configme.properties.PropertyInitializer.newBeanProperty
import ch.jalu.configme.properties.PropertyInitializer.newListProperty
import ch.jalu.configme.properties.PropertyInitializer.newProperty
import me.clip.voteparty.config.objects.Command
import me.clip.voteparty.config.objects.Commands
import me.clip.voteparty.config.objects.CommandsReward

object PartySettings : SettingsHolder
{
	@JvmField
	val VOTES_NEEDED = newProperty("party.votes_needed", 50)
	
	@JvmField
	val DISABLED_WORLDS = newListProperty("party.disabled_worlds", "")
	
	@JvmField
	val OFFLINE_VOTES = newProperty("party.offline_votes", true)
	
	@JvmField
	val MAX_REWARDS_PER_PLAYER = newProperty("party.max_rewards_per_player", 1)
	
	@JvmField
	val START_DELAY = newProperty("party.start_delay", 15)
	
	@JvmField
	val REWARD_COMMANDS: Property<CommandsReward> = newBeanProperty(CommandsReward::class.java, "party.reward_commands", CommandsReward(1, listOf(Command(50, "eco give %player_name% 100"), Command(50, "give %player_name% DIAMOND 6"), Command(50, "give %player_name% IRON_INGOT 12"))))
	
	@JvmField
	val GUARANTEED_REWARDS: Property<Commands> = newBeanProperty(Commands::class.java, "party.guaranteed_rewards", Commands(true, listOf("eco give %player_name% 10", "give %player_name% STEAK 8")))
	
	@JvmField
	val PRE_PARTY_COMMANDS: Property<Commands> = newBeanProperty(Commands::class.java, "party.pre_party_commands", Commands(true, listOf("broadcast Party will start soon!")))
	
	@JvmField
	val PARTY_COMMANDS: Property<Commands> = newBeanProperty(Commands::class.java, "party.party_commands", Commands(true, listOf("broadcast Party will start soon!")))
}