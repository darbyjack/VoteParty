package me.clip.voteparty.conf.sections

import ch.jalu.configme.Comment
import ch.jalu.configme.SettingsHolder
import ch.jalu.configme.properties.Property
import ch.jalu.configme.properties.PropertyInitializer.newBeanProperty
import ch.jalu.configme.properties.PropertyInitializer.newProperty
import me.clip.voteparty.conf.objects.Command
import me.clip.voteparty.conf.objects.Commands
import me.clip.voteparty.conf.objects.PermCommands
import me.clip.voteparty.conf.objects.PermRewards
import me.clip.voteparty.conf.objects.RewardsPerEvent

internal object VoteSettings : SettingsHolder
{
	
	@JvmField
	@Comment("Configuration for chance rewards to be given for voting.", "Add as many commands as you want, set their chance, and choose the max amount a player can earn!")
	val PER_VOTE_REWARDS: Property<RewardsPerEvent> = newBeanProperty(RewardsPerEvent::class.java, "voting.per_vote_rewards", RewardsPerEvent(true, 1, listOf(Command(50, "eco give %player_name% 100"), Command(70, "give %player_name% STEAK 10"))))
	
	@JvmField
	@Comment("Configuration for extra commands to be executed on players who have specific permission nodes")
	val PERMISSION_VOTE_REWARDS: Property<PermRewards> = newBeanProperty(PermRewards::class.java, "voting.permission-rewards", PermRewards(true, listOf(PermCommands("my.special.permission", listOf("eco give %player_name% 500")))))
	
	@JvmField
	@Comment("A list of rewards that will ALWAYS be given to a player for voting")
	val GUARANTEED_REWARDS: Property<Commands> = newBeanProperty(Commands::class.java, "voting.guaranteed_rewards", Commands(true, listOf("eco give %player_name% 10", "give %player_name% STEAK 8")))
	
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
	
}